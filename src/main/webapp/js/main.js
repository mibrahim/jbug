function checkUser()
{
    window.JBugUser = $.cookie("jbug.useremail");

    if (JBugUser === null)
    {
        getUserEmail();
    } else {
        window.JBugUserMd5 = $.md5(JBugUser);
        updateUserGravatar();
    }
}

function getUserEmail()
{
    if (window.JBugUser === null)
        window.JBugUser = "";
    window.JBugUser = prompt("What's your email?", window.JBugUser);
    window.JBugUserMd5 = $.md5(JBugUser);
    $.cookie("jbug.useremail", JBugUser);
    updateUserGravatar();
}

function updateUserGravatar()
{
    //alert("md5:"+window.JBugUserMd5);
    $("#grv").html("<img align='top' src='http://www.gravatar.com/avatar/" + window.JBugUserMd5 + "?s=28'/>");
}