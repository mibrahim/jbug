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

function setMainContentHeight()
{
    var winW = 630, winH = 460;
    if (document.body && document.body.offsetWidth) {
        winW = document.body.offsetWidth;
        winH = document.body.offsetHeight;
    }
    if (document.compatMode === 'CSS1Compat' &&
            document.documentElement &&
            document.documentElement.offsetWidth) {
        winW = document.documentElement.offsetWidth;
        winH = document.documentElement.offsetHeight;
    }
    if (window.innerWidth && window.innerHeight) {
        winW = window.innerWidth;
        winH = window.innerHeight;
    }
    height = winH-10-60;
    $("#main").height(height+"px");
}