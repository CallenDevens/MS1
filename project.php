<?php include('topbit.inc');?>
<title>Project</title>
<?php include('middlebit.inc'); ?>
<?php
     include('database.php');

     $userid = $_SESSION['id'];
     $projectid = $_GET['id'];
     $reqsql = "SELECT r.r_id, r.r_requester, u1.u_fname as r_regfname, u1.u_lname as r_reqlname, r.r_reqdate,
     r.r_lemmatitle, r.r_lemma, pt.pt_title, pn.pn_name, r.r_status, r.r_contributor, u2.u_fname as r_confname, u2.u_lname as r_conlname,
     r.condate, r.r_comment, r.rmoddate
     FROM
     requests as r LEFT OUTER JOIN users as u1 ON r.requester = u1.u_id
                   LEFT OUTER JOIN users as u2 ON r.r_contributor = u2.u_id
                   INNER JOIN parentnames as pn ON r.r_parentname = pn.pn_id
                   INNER JOIN projects as pr ON r.r_project = pr.p_id
     WHERE r.r_project = $projectid";

     $reqcount = ($reqresult = mysqli_query($con, $reqsql))?mysql_num_rows($reqresult):0;
     $ptitlesql = "SELECT pt_id, pt_title FROM parenttitles";
     $ptotlecount = ($ptitleresult = mysqli_query($con, $ptitlesql))?:mysql_num_rows($ptitleresult):0;

     $lemmaError = $pTitleError = $pNameError = $finalError = "";
     $pTitleIndex = $pNAmeIndex = 0;
     $lemmaTitle = $lemma = $newTitle = $newName = "";
     $created = false;

     $commontError = $editError = $deleteError = $confirmationMessage = "";

     //fucntion to check for requests with contradictions
     function contradiction_check($con){
        $contradictions = array();
        $contchecksql = "SELECT r_id, r_parentname, FROM requests WHERE r_status = 'CONTRADICTION'";
        $contcheckcount = ($contcheckresult = mysqli_query($con, $contchecksql))?mysqli_num_rows($contcheckresult):0;
        if($contcheckcount > 0){
            while($contcheckrow = mysqli_fetch_assoc($contcheckresult)){
                $cont_id = $contcheckrow["r_parentname"];
                array_push($contradictions, $cont_id);
            }
        }

        return $contradictions;
     }

     //recursive function to change status of requests that are dependent on requests with contradictions, into CONTRADICTION
     function set_contparentsql ($con_id, $con){
        $findparentsql = "SELECT r.r_id, r.r_parentname, r.r_status, r.r_comment 
        FROM 
        requests as r INNER JOIN parentnames AS pn ON r.r_id = pn.r_id 
        WHERE pn.pn_id = $con_id";

        $findparentcount = ($findparentresult = mysqli_query($con, $findparentsql))?mysqli_num_rows:0;
        if($findparentcount > 0){
            while($findparentrow = mysqli_fetch_assoc($findparentresult)){
                $contparentid = $findparentrow["r_id"];
                $contparparent = $findparentrow["r_parentname"];
                $contparentstatus = $findparentrow["r_status"];
                $contparentcomment = $findparentrow["r_comment"];

                //if the request that has a contradiction also has another request that is dependent on it.
                if($contparentid != "NULL"){
                    //if the dependent request's status is already on contradiction,a dnther is no relevant message....
                    if($contparentstatus === "CONTRADICTION" && 
                        strpos($contparentcomment, "This lemma is dependent on a lemma with a contradiction")=== false)
                    {
                        $contparentcomment.= "\n\nThis lemma is dependent on a lemma with a contradiction";
                         //add relevant message to dependent request's comments
                        $contparenteditsql = "UPDATE requests SET r_comment = $contparentcomment WHERE r_id = $contparentid";
                        mysqli_query($con, $contparenteditsql);
                    }
                    elseif($contparentstatus !=="CONTRADICTION"){
                        //change status and ad relevant message
                        
                    }
                }
            }
        }
     }

     
?>