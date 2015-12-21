<?php include('topbit.inc'); ?>
<title>Project</title>
<?php include('middlebit.inc'); ?>
<?php
	include('database.php');
	
	$userid = $_SESSION['id'];
	$projectid = $_GET['id'];
	$reqsql = "SELECT r.r_id, r.r_requester, u1.u_fname as r_reqfname, u1.u_lname as r_reqlname, r.r_reqdate, r.r_lemmatitle, r.r_lemma, pt.pt_title, pn.pn_name, r.r_status, r.r_contributor, u2.u_fname as r_confname, u2.u_lname as r_conlname, r.r_condate, r.r_comment, r.r_moddate FROM requests as r LEFT OUTER JOIN users as u1 ON r.r_requester = u1.u_id LEFT OUTER JOIN users as u2 ON r.r_contributor = u2.u_id INNER JOIN parenttitles as pt ON r.r_parenttitle = pt.pt_id INNER JOIN parentnames as pn ON r.r_parentname = pn.pn_id INNER JOIN projects as pr ON r.r_project = pr.p_id WHERE r.r_project = '$projectid'";
	$reqcount = ($reqresult = mysqli_query($con, $reqsql))?mysqli_num_rows($reqresult):0;
	$ptitlesql = "SELECT pt_id, pt_title FROM parenttitles";
	$ptitlecount = ($ptitleresult = mysqli_query($con, $ptitlesql))?mysqli_num_rows($ptitleresult):0;
	
	$lemmaError = $pTitleError = $pNameError = $finalError = "";
	$pTitleIndex = $pNameIndex = 0;
	$lemmaTitle = $lemma = $newTitle = $newName = "";
	$created = false;
	
	$commentError = $editError = $deleteError = $confirmationMessage = "";
	// function to check for requests with contradictions
	function contradiction_check($con){
		$contradictions = array();
		$contchecksql = "SELECT r_id, r_parentname FROM requests WHERE r_status = 'CONTRADICTION'";
		$contcheckcount = ($contcheckresult = mysqli_query($con, $contchecksql))?mysqli_num_rows($contcheckresult):0;
		if($contcheckcount > 0){
			while($contcheckrow = mysqli_fetch_assoc($contcheckresult)){
				$cont_id = $contcheckrow["r_parentname"];
				// insert id of requests with contradictions into array
				array_push($contradictions, $cont_id);
			}
		}
		return $contradictions;
	}
	// recursive function to change status of requests that are dependent on requests with contradictions, into "CONTRADICTION"
	function set_contparents($con_id, $con){
		$findparentsql = "SELECT r.r_id, r.r_parentname, r.r_status, r.r_comment FROM requests AS r INNER JOIN parentnames AS pn ON r.r_id = pn.r_id WHERE pn.pn_id = '$con_id'";
		$findparentcount = ($findparentresult = mysqli_query($con, $findparentsql))?mysqli_num_rows($findparentresult):0;
		if($findparentcount > 0){
			// take details of the parent request that has a contradiction
			while($findparentrow = mysqli_fetch_assoc($findparentresult)){
				$contparentid = $findparentrow["r_id"];
				$contparparent = $findparentrow["r_parentname"];
				$contparentstatus = $findparentrow["r_status"];
				$contparentcomment = $findparentrow["r_comment"];
			}
			// if the request that has a contradiction also has another request that is dependent on it...
			if($contparentid != "NULL"){
				// if the dependent request's status is already on contradiction, and there is no relevant message...
				if($contparentstatus === "CONTRADICTION" && strpos($contparentcomment, "This lemma is dependent on a lemma with a contradiction") === false){
					$contparentcomment .= "\n\nThis lemma is dependent on a lemma with a contradiction";
					// add relevant message to dependent request's comments
					$contparenteditsql = "UPDATE requests SET r_comment='$contparentcomment' WHERE r_id='$contparentid'";
					mysqli_query($con, $contparenteditsql);
				}
				// if the dependent request's status is not "CONTRADICTION"...
				elseif($contparentstatus !== "CONTRADICTION"){
					// change status and add relevant message
					$contparenteditsql = "UPDATE requests SET r_status='CONTRADICTION', r_comment='This lemma is dependent on a lemma with a contradiction' WHERE r_id='$contparentid'";
					mysqli_query($con, $contparenteditsql);
				}
				if($contparparent != "NULL"){
					// recursive function call. if the dependent request is parent to another request, call this function on that request as well
					set_contparents($contparparent, $con);
				}
			}
		}
	}
	
	if($_SERVER["REQUEST_METHOD"] == "POST" && isset($_GET['action'])){
		// php handling of edit status request
		if($_GET['action'] == "edit" && isset($_GET['editid'])){
			$reqID = $_GET['editid'];
			$newStatus = $_POST["newStatus"];
			$newComment = trim($_POST["newComment"]);
			if ($newStatus == "SOLVED" || $newStatus == "CONTRADICTION"){
				if ($newComment == ""){
					$commentError = "Please enter solution or contradiction in comment box";
				}
			}
			if ($commentError == ""){
				// if no errors in edit status request...
				if($newStatus == "REQUESTED"){
					$editsql = "UPDATE requests SET r_status='$newStatus', r_contributor=NULL, r_condate=NULL, r_comment=NULL WHERE r_id='$reqID'";
				}
				else{
					$editsql = "UPDATE requests SET r_status='$newStatus', r_contributor='$userid', r_condate=NOW(), r_comment='$newComment' WHERE r_id='$reqID'";
					$subscribesql = "INSERT INTO usersrequests (u_id, r_id) VALUES ('$userid', '$reqID');";
				}
				try{
					mysqli_query($con, $subscribesql);
					mysqli_query($con, $editsql);
					// send email notification to supervisor. change supervisor's email address and other inputs as necessary
					$emailAddress = "anooooos-2oo9@hotmail.com"; // the email address we are sending to
					$subject = "Changes"; // the title of the email
					$message = "the status of the project has been changed"; // the message we are sending

					if(mail($emailAddress,$subject,$message))
					{
						$confirmationMessage = "Supervisor has been notified of changes.";
					}
					else
					{
						$confirmationMessage = "Supervisor was NOT notified. Email system broken.";
					}
					$reqcount = ($reqresult = mysqli_query($con, $reqsql))?mysqli_num_rows($reqresult):0;
				}
				catch(Exception $e){
					$editError = "Error editing request";
				}
			}
		}
	}
	
	if(isset($_GET['action'])){
		// php handling of deletion requests
		if($_GET['action'] == "delete" && isset($_GET['deleteid'])){
			$reqID = $_GET['deleteid'];
			$checksuccess = false;
			// check if user that made request is really the requester
			$checksql = "SELECT r_id, r_requester FROM requests WHERE r_requester = '$userid'";
			$checkcount = ($checkresult = mysqli_query($con, $checksql))?mysqli_num_rows($checkresult):0;
			if($checkcount > 0){
				while($checkrow = mysqli_fetch_assoc($checkresult)){
					if($checkrow["r_id"] == $reqID){
						$checksuccess = true;
						break;
					}
				}
			}
			if($checksuccess == true){
				$deletesql = "DELETE FROM requests WHERE r_id = '$reqID'";
				try{
					mysqli_query($con, $deletesql);
					// send email notification to supervisor. change supervisor's email address and other inputs as necessary
					$emailAddress = "anooooos-2oo9@hotmail.com"; // the email address we are sending to
					$subject = "Delete"; // the title of the email
					$message = "The project has been deleted"; // the message we are sending

					if(mail($emailAddress,$subject,$message))
					{
						$confirmationMessage = "Supervisor has been notified of changes.";
					}
					else
					{
						$confirmationMessage = "Supervisor was NOT notified. Email system broken.";
					}
				}
				catch(Exception $e){
					$deleteError = "Error deleting request";
				}
			}
			else{
				$deleteError = "Cannot delete a request that isn't yours"; 
			}
		}
	}
	
	if($ptitlecount > 0){?>
		<script type="text/javascript">
			var titles = [];
		</script>
		<?php while($ptitlerow = mysqli_fetch_assoc($ptitleresult)){
			$currentID = $ptitlerow["pt_id"];
			$currentTitle = $ptitlerow["pt_title"];
			$php_array = array();
			// find parent names for pre-population function below
			$namesql = "SELECT pn_id, pn_name FROM parentnames";
			$namecount = ($nameresult = mysqli_query($con, $namesql))?mysqli_num_rows($nameresult):0;
			if($namecount > 0){
				while($namerow = mysqli_fetch_assoc($nameresult)){
					$currentNameID = $namerow["pn_id"];
					$currentName = $namerow["pn_name"];
					array_push($php_array, ($currentNameID."|".$currentName));
				}
			}
			if($namecount > 0){
				?>
				<script type="text/javascript">
					var id_<?php echo $currentID?> = <?php echo json_encode($php_array);?>;
					titles[titles.length] = "<?php echo $currentID?>";
				</script>
			<?php }
		}?>
		<script type="text/javascript">
			// javascript function to pre-populate dropdown boxes for purposes of creating a new request
			// get rid of this function and replace with AJAX/jQuery/etc. in future improvements
			function clearBox(selectbox){
				for (i = selectbox.options.length-1;i>=2;i--){
					selectbox.remove(i);
				}
			}
			function fillBox(){
				inputParentTitle();
				var selected = document.getElementById("parentTitle").value;
				console.log(selected);
				var id = "";
				for (i = 0; i < titles.length; i++){
					if(selected === titles[i]){
						id = titles[i];
						break;
					}
				}
				console.log(id);
				if(id !== ""){
					var names = document.getElementById("parentName");
					clearBox(names);
					var varname = "id_"+id;
					for (j = 0; j < eval(varname).length; j++){
						var idname = eval(varname)[j];
						console.log(idname);
						var idname = idname.split("|");
						var nameID = idname[0];
						var name = idname[1];
						var option = document.createElement("option");
						option.text = name;
						option.value = nameID;
						names.appendChild(option);
					}
				}
			}
		</script>
	<?php }
	// php handling for new requests
	if($_SERVER["REQUEST_METHOD"] == "POST" && !isset($_GET['action'])){
		if(empty($_POST["lemma"])){
			$lemmaError = "Lemma is required";
		}
		if(empty($_POST["parentTitle"])){
			$pTitleError = "Parent title is required";
			
		}
		else{
			if($_POST["parentTitle"] == "createNewTitle"){
				if(empty($_POST["newTitle"])){
					$pTitleError = "Parent title is required";
					$pTitleIndex = 1;
				}
			}
		}
		if(empty($_POST["parentName"])){
			$pNameError = "Parent name is required";
		}
		else{
			if($_POST["parentName"] == "createNewName"){
				if(empty($_POST["newName"])){
					$pNameError = "Parent name is required";
					$pNameIndex = 1;
				}
			}
		}
		// if new request is using a new parent title and parent name
		if(!empty($_POST["newTitle"]) && !empty($_POST["newName"])){
			$nt = $_POST["newTitle"];
			$nn = $_POST["newName"];
			$newtitlesql = "SELECT * FROM parenttitles WHERE pt_title = '$nt'";
			if($newtitleresult = mysqli_query($con, $newtitlesql)){
				$pTitleError = "Parent title already exists";
			}
			$newnamesql = "SELECT * FROM parentnames WHERE pn_name = '$nn'";
			if($newnameresult = mysqli_query($con, $newnamesql)){
				$pNameError = "Parent name already exists";
			}
			else{
				$createtitlesql = "INSERT INTO parenttitles (pt_id, pt_title) VALUES (NULL, '$nt');";
				$createnamesql = "INSERT INTO parentnames (pn_id, pn_name) VALUES (NULL, '$nn');";
				try{
					mysqli_query($con, $createtitlesql);
					mysqli_query($con, $createnamesql);
					$finaltitlesql = "SELECT pt_id FROM parenttitles ORDER BY pt_id DESC LIMIT 1;";
					if($finaltitleresult = mysqli_query($con, $finaltitlesql)){
						while($finaltitlerow = mysqli_fetch_assoc($finaltitleresult)){
							$finalTitle = $finaltitlerow['pt_id'];
						}
					}
					$finalnamesql = "SELECT pn_id FROM parentnames ORDER BY pn_id DESC LIMIT 1;";
					if($finalnameresult = mysqli_query($con, $finalnamesql)){
						while($finalnamerow = mysqli_fetch_assoc($finalnameresult)){
							$finalName = $finalnamerow['pn_id'];
						}
					}
				}
				catch(Exception $e){
					$pTitleError = $pNameError = "Could not create new parent title or name, please try again";
				}
			}
		}
		// if new request is using a new parent name, but an existing parent title
		if(!empty($_POST["newName"]) && empty($_POST["newTitle"])){
			$pt = $_POST['parentTitle'];
			$nn = $_POST['newName'];
			$newnamesql = "SELECT * FROM parentnames WHERE pn_name = '$nn'";
			if($newnameresult = mysqli_query($con, $newnamesql)){
				$pNameError = "Parent name already exists";
			}
			else{
				$createnamesql = "INSERT INTO parentnames (pn_id, pn_name) VALUES (NULL, '$nn');";
				try{
					mysqli_query($con, $createnamesql);
					$finalnamesql = "SELECT pn_id FROM parentnames ORDER BY pn_id DESC LIMIT 1;";
					if($finalnameresult = mysqli_query($con, $finalnamesql)){
						while($finalnamerow = mysqli_fetch_assoc($finalnameresult)){
							$finalName = $finalnamerow['pn_id'];
						}
						$finalTitle = $_POST["parentTitle"];
					}
				}
				catch(Exception $e){
					$pTitleError = $pNameError = "Could not create new parent name, please try again";
				}
			}
		}
		// if new request is using an existing parent name and title
		if(empty($_POST["newName"]) && empty($_POST["newTitle"])){
			$finalTitle = $_POST['parentTitle'];
			$finalName = $_POST['parentName'];
		}
		if($lemmaError == "" && $pTitleError == "" && $pNameError == ""){
			$lt = addslashes($_POST['lemmaTitle']);
			$le = addslashes($_POST['lemma']);
			$insertparentsql = "INSERT INTO parentnames (pn_id, pn_name) VALUES (NULL, '$lt');";
			$requestsql = "INSERT INTO requests (r_id, r_project, r_requester, r_reqdate, r_lemmatitle, r_lemma, r_parenttitle, r_parentname, r_status, r_contributor, r_condate, r_comment, r_moddate) VALUES (NULL, '$projectid', '$userid', NOW(), '$lt', '$le', '$finalTitle', '$finalName', 'REQUESTED', NULL, NULL, NULL, NOW());";
			try{
				$requestresult = mysqli_query($con, $requestsql);
				$insertparentresult = mysqli_query($con, $insertparentsql);
				$finalrequestsql = "SELECT r_id FROM requests ORDER BY r_id DESC LIMIT 1;";
				if($finalrequestresult = mysqli_query($con, $finalrequestsql)){
					while($finalrequestrow = mysqli_fetch_assoc($finalrequestresult)){
						$finalRequest = $finalrequestrow['r_id'];
					}
					$subscribesql = "INSERT INTO usersrequests (u_id, r_id) VALUES ('$userid', '$finalRequest');";
					mysqli_query($con, $subscribesql);
				}
				echo "New request created. <br/> Page will be refreshed now. <br/>";
				$url = "requests.php?id=".$projectid;
				header( "refresh:5;url=$url" );
				echo "<a href=\"$url\">Click here if page does not refresh in 5 seconds.</a><br/>";
			}
			catch(Exception $e){
				$finalError = "Could not create new request, please try again";
			}
		}
	}
	// if new request creation failed, insert old input data into new request form
	if($_SERVER["REQUEST_METHOD"] == "POST" && $created == false){
		if(isset($_POST['lemma'])){
			$lemma = trim($_POST['lemma']);
		}
		if(isset($_POST['newTitle'])){
			$newTitle = trim($_POST['newTitle']);
		}
		if(isset($_POST['newName'])){
			$newName = trim($_POST['newName']);
		}
	}
	// set variables for different row colours in css
	$colors = array(
		"CONTRADICTION" => "red",
		"SOLVED" => "limegreen"
	);
	
	if($reqcount > 0){
		// call function to check contradiction and change status of contradictory parent requests
		$contradictions = contradiction_check($con);
		foreach($contradictions as $con_id){
			set_contparents($con_id, $con);
		}
		?>
		<div class="requests">
		<?php echo $editError."<br/>";?>
		<table style="width:100%">
			<tr>
				<th>Request from</th>
				<th>Lemma</th>
				<th>Used in</th>
				<th>Status</th>
			</tr>
			<?php while($row = mysqli_fetch_assoc($reqresult)){?>
				<!-- set background colour css of table row depending on request's status -->
				<tr style="background-color: <?php echo (isset($colors[$row["r_status"]])?$colors[$row["r_status"]]:"white"); ?>">
					<form method="post" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]."?id=".$projectid."&action=edit&editid=".$row["r_id"]); ?>">
						<td><?php echo $row["r_reqfname"]." ".$row["r_reqlname"];?><br/><?php echo $row["r_reqdate"];?></td>
						<td><pre>lemma <?php echo htmlspecialchars($row["r_lemmatitle"]);?>:</pre><pre><?php echo htmlspecialchars($row["r_lemma"]);?></pre><br/><br/>Last modified date: <em><?php echo $row["r_moddate"];?></em></td>
						<td><pre><?php echo $row["pt_title"];?>:<br/><?php echo $row["pn_name"];?></pre></td>
						<td><span id="status<?php echo $row["r_id"];?>"><?php echo $row["r_status"];?></span>
						<span id="statusin<?php echo $row["r_id"];?>" class="editArea">
						<select name="newStatus" id="newStatus">
						<option value="REQUESTED" <?php echo ($row["r_status"]=="REQUESTED")?('selected'):('');?>>REQUESTED</option>
						<option value="IN PROGRESS" <?php echo ($row["r_status"]=="IN PROGRESS")?('selected'):('');?>>IN PROGRESS</option>
						<option value="SOLVED" <?php echo ($row["r_status"]=="SOLVED")?('selected'):('');?>>SOLVED</option>
						<option value="CONTRADICTION" <?php echo ($row["r_status"]=="CONTRADICTION")?('selected'):('');?>>CONTRADICTION</option>
						</select>
						</span>
						<br/><?php echo ($row["r_confname"]===NULL&&$row["r_conlname"]===NULL)?(''):($row['r_confname'].' '.$row['r_conlname'].'<br/>'.$row['r_condate']);?><br/><br/><span id="comment<?php echo $row["r_id"];?>"><pre><?php echo ($row['r_comment']===NULL)?(''):($row["r_comment"]);?></pre></span>
						<span id="commentin<?php echo $row["r_id"];?>" class="editArea">
						Comments:<br/>
						<textarea name="newComment"><?php echo ($row["r_comment"]===NULL)?(''):($row["r_comment"]);?></textarea>
						</span>
						<br/><br/><div id="editin<?php echo $row["r_id"];?>" class="editButtons"><input type="submit" value="Save Changes"/></div>
						<div class="requestButtons"><input type="button" value="Change Status" onClick='changeStatus(<?php echo $row["r_id"];?>)'/><?php if($_SESSION['id'] == $row["r_requester"]){?> <input type="button" value="Delete Request" onClick='deleteRequest(<?php echo $projectid.",".$row["r_id"];?>)'/><?php } if($_SESSION['id'] != $row["r_requester"] && $_SESSION['id'] != $row["r_contributor"]){?><input type="checkbox" name="Subscription" value="true"/>Subscribe<?php }?></div></td>
					</form>
				</tr>
			<?php }?>
		</table>
		</div>
		<br/>
	<?php }
	else{?>
	<h2>No requests in this project.</h2>
	<?php }?>
	<input type="button" onClick='createRequest();' value="New Request"/>
	<br/><br/>
	<div class="creation" id="createRequest">
	<?php if($_SERVER["REQUEST_METHOD"] == "POST" && $created == false && !isset($_GET['action'])){
		if(isset($_POST['p_title'])){
			$pTitle = trim($_POST['p_title']);
		}?>
		<script type="text/javascript">
			createRequest();
		</script>
	<?php }?>
	<!-- form for creating a new request -->
	<form method="post" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]."?id=".$projectid); ?>">
		<h2>Create a new request:</h2>
		<br/>
		<span class="error"><?php echo $finalError;?></span>
		<br/>
		Lemma Title:<br/>
		<input type="text" name="lemmaTitle" value="<?php echo $lemmaTitle ?>"/>
		<br/>
		Lemma:<br/>
		<textarea name="lemma"><?php echo $lemma ?></textarea>
		<span class="error"><?php echo $lemmaError;?></span>
		<br/><br/>
		Used in:<br/>
		
		<select name="parentTitle" id="parentTitle" onChange="fillBox();">
		<option value="" <?php echo ($pTitleIndex==0)?('selected'):('');?>>Select a parent title</option>
		<option value="createNewTitle" <?php echo ($pTitleIndex==1)?('selected'):('');?>>Create new parent title</option>
		<?php if($ptitlecount > 0){
			$ptitleresult2 = mysqli_query($con, $ptitlesql);
			while($ptitlerow2 = mysqli_fetch_assoc($ptitleresult2)){?>
				<option value="<?php echo $ptitlerow2["pt_id"];?>"><?php echo $ptitlerow2["pt_title"];?></option>
			<?php }
		}?>
		</select>
		<span class="hiddenInput" id="inputParentTitle"><br/>New title:<br/>
		<input type="text" name="newTitle" value="<?php echo $newTitle ?>"/></span>
		<span class="error"><?php echo $pTitleError;?></span>
		<br/><br/>
		<span class="hiddenInput" id="parentNameArea">
		<select name="parentName" id="parentName" onChange="inputParentName();">
		<option value="" <?php echo ($pNameIndex==0)?('selected'):('');?>>Select a parent name</option>
		<option value="createNewName" <?php echo ($pNameIndex==1)?('selected'):('');?>>Create new parent name</option>
		</select>
		<span class="hiddenInput" id="inputParentName"><br/>New name:<br/>
		<input type="text" name="newName" value="<?php echo $newName ?>"/></span>
		<span class="error"><?php echo $pNameError;?></span>
		</span>
		<br/><br/>
		<input type="submit" value="Create Request">
	</form>
	<?php if($_SERVER["REQUEST_METHOD"] == "POST" && $created == false){
		if($pTitleIndex==1){?>
			<script type="text/javascript">
				inputParentTitle();
			</script>
		<?php }
		if($pNameIndex==1){?>
			<script type="text/javascript">
				inputParentName();
			</script>
		<?php }
	}?>
	</div>
	<?php 
		// recursive function to get dependency tree in text format
		function get_children($par_id, $par_title, $con, &$text){
			// find requests that are dependent on above request (par_id)
			$childrensql = "SELECT r_id, r_lemmatitle, r_parentname FROM requests WHERE r_parentname = '$par_id'";
			$childrencount = ($childrenresult = mysqli_query($con, $childrensql))?mysqli_num_rows($childrenresult):0;
			if($childrencount > 0){
				$text .= "[";
				$loopcount = 0;
				// loop through each dependent request
				while($childrow = mysqli_fetch_assoc($childrenresult)){
					// record relevant data from dependent requests
					$childreq_id = $childrow["r_id"];
					$childlemmatitle = $childrow["r_lemmatitle"];
					// find dependent request in parentnames table
					$childinparentsql = "SELECT pn_id, pn_name FROM parentnames WHERE pn_name = '$childlemmatitle'";
					$childinparentresult = mysqli_query($con, $childinparentsql);
					while($childinparentrow = mysqli_fetch_assoc($childinparentresult)){
						$childinparentid = $childinparentrow["pn_id"];
					}
					// find requests that are dependent on the dependent request
					$childchildrensql = "SELECT r_lemmatitle, r_parentname FROM requests WHERE r_parentname = '$childinparentid'";
					$childchildrencount = ($childchildrenresult = mysqli_query($con, $childchildrensql))?mysqli_num_rows($childchildrenresult):0;
					if($childchildrencount > 0){
						$text .= $childlemmatitle;
						// recursive function call on request that is dependent on the dependent request
						get_children($childinparentid, $childlemmatitle, $con, $text);
					}
					else{
						$text .= $childlemmatitle;
						if($loopcount+1 < $childrencount){
							$text .= ", ";
						}
					}
					$loopcount++;
				}
				$text .= "]";
				return $text;
			}
			else{
				return "";
			}
		}
		$requestsql = "SELECT r_id FROM requests WHERE r_project = '$projectid'";
		$requestcount = ($requestresult = mysqli_query($con, $requestsql))?mysqli_num_rows($requestresult):0;
		// list dependencies if there are requests available
		if($requestcount > 0){?> 
			<br/>
			<h2>Dependencies:</h2><?php 
			$parsql = "SELECT pn_id, pn_name FROM parentnames";
			$parcount = ($parresult = mysqli_query($con, $parsql))?mysqli_num_rows($parresult):0;
			if($parcount > 0){?>
				<ol type="1">
				<?php while($parrow = mysqli_fetch_assoc($parresult)){
					$currentparid = $parrow["pn_id"];
					$currentparname = $parrow["pn_name"];
					$text = $currentparname;
					$currentparchildrensql = "SELECT r_id FROM requests WHERE r_parentname = '$currentparid'";
					$currentparchildrencount = ($currentparchildrenresult = mysqli_query($con, $currentparchildrensql))?mysqli_num_rows($currentparchildrenresult):0;
					if($currentparchildrencount > 0){
						?><li><?php echo get_children($currentparid, $currentparname, $con, $currentparname);?></li><br/>
					<?php }
					}?>
				</ol>
			<?php }
		}
	?>
	<br/>
	<!-- twitter and facebook sharing code -->
	<a href="https://twitter.com/share" class="twitter-share-button" data-text="Check this out" data-size="large">Tweet</a>
	<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script>
	<div class="fb-share-button" data-href="http://localhost/Anas/requests.php?id=1" data-layout="button_count"></div>
	<div id="fb-root"></div>
	<script>(function(d, s, id) {
	  var js, fjs = d.getElementsByTagName(s)[0];
	  if (d.getElementById(id)) return;
	  js = d.createElement(s); js.id = id;
	  js.src = "//connect.facebook.net/en_GB/sdk.js#xfbml=1&version=v2.3";
	  fjs.parentNode.insertBefore(js, fjs);
	}(document, 'script', 'facebook-jssdk'));</script>
	<br/>
<?php include('endbit.inc'); ?>