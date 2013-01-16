// Global Variables
var jBugUser = "";
var mainWindowHeight = 100;
var bugList = "";
var currentPage;
var winW = 630, winH = 460;
var pageToDisplay;
var params;
var bugId;

// Location update detector
var oldLocation = location.href;
setInterval(function() {
    if (location.href !== oldLocation) {
	oldLocation = location.href;
	showCurrentPage();
    }
}, 100); // check every second

function extractURLVariables()
{
    var prmarr = document.location.hash.substr(1).split("&");
    params = {};

    for (var i = 0; i < prmarr.length; i++) {
	var tmparr = prmarr[i].split("=");
	params[tmparr[0]] = tmparr[1];
    }
}

function checkUser()
{
    jBugUser = $.cookie("jbug.useremail");

    if (jBugUser === null)
    {
	getUserEmail();
    } else {
	updateUserGravatar();
    }
}

function getUserEmail()
{
    if (jBugUser === null)
	jBugUser = "";
    jBugUser = prompt("What's your email?", window.jBugUser);
    $.cookie("jbug.useremail", jBugUser);
    updateUserGravatar();
}

function updateUserGravatar()
{
    $("#grv").html(getUserGravatarImg(jBugUser, 28));
}

function getUserGravatarImg(email, size)
{
    if (size === null || size === undefined)
	size = 28;
    return "<img class='usergravatar' align='top' style='width:" + size + "px;height:" + size + "px;' src='http://www.gravatar.com/avatar/" + $.md5(email) + "?d=wavatar&s=" + size + "'/>";
}

function setMainContentHeight()
{
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
    mainWindowHeight = winH - 10 - 60;
    $("#main").height(mainWindowHeight + "px");
    if (bugList.length > 1)
	showCurrentPage();
}

function updateBugStatusBar()
{
    $.ajax({
	url: "data.jsp?get=openbugcount",
	context: document.body,
	async: true
    }).done(function(data) {
	$("#openbugs").html(data);
    });
}

var buglist = "";

function updateOpenBugsList()
{
    $.ajax({
	url: "data.jsp?get=openbugids",
	context: document.body,
	async: false
    }).done(function(data) {
	bugList = data;
    });
}

function showOpenBugs()
{
    $.ajax({
	url: "data.jsp?get=openbugids",
	context: document.body
    }).done(function(data) {
	bugList = data;
	showCurrentPage();
    });
}

function pageLink(page, pageText, isCurrent)
{
    html = " <a href='#do=bugpage&page=" + page + "' class='btn " + ((isCurrent === true) ? "btn-yellow" : "btn-red") + "'>" + pageText + "</a>";
    return html;
}

function showCurrentPage()
{
    extractURLVariables();

    if (params !== undefined)
    {
	if (params['do'] !== undefined)
	    pageToDisplay = params['do'];
	else
	    pageToDisplay = undefined;
	if (params['page'] !== undefined)
	    currentPage = parseInt(params['page']);
	else
	    currentPage = undefined;
	if (params['bugid'] !== undefined)
	    bugId = (params['bugid'] !== "new") ? parseInt(params['bugid']) : "new";
	else
	    bugId = undefined;
    }

    if (currentPage === undefined)
	currentPage = 1;
    if (pageToDisplay === undefined)
	pageToDisplay = "";

    switch (pageToDisplay)
    {
	case "":
	case "bugpage":
	    showCurrentBugPage();
	    break;
	case "bugdetails":
	    showBugDetails();
	    break;
	case "bugedit":
	    bugEdit();
	    break;
    }
}

function showCurrentBugPage()
{
    if (bugList === undefined || bugList.length === 0 || bugList === "Not found")
	return;

    bugs = bugList.split(",");
    pageSize = Math.floor((mainWindowHeight - 90) / 33);
    nPages = Math.ceil(bugs.length / pageSize);

    if (currentPage > nPages)
	currentPage = nPages;

    navBar = "<br/><center> ";
    navBar += pageLink(1, "<i class='icon-fast-backward'></i>", false);
    start = 0;
    regions = [[1, 3], [currentPage - 3, currentPage + 3], [nPages - 2, nPages]];

    for (z = 0; z < regions.length - 1; z++)
    {
	if (regions[z][1] > regions[z + 1][0]) {
	    regions[z][1] = regions[z + 1][1];
	    for (k = z + 1; k < regions.length - 1; k++)
		regions[k] = regions[k + 1];
	    regions.splice(regions.length - 1, 1);
	    z--;
	}
    }

    for (z = 0; z < regions.length; z++)
    {
	if (z !== 0)
	    navBar += "&nbsp;&nbsp;&nbsp;<i class='icon-circle navdot'></i> <i class='icon-circle navdot'></i> <i class='icon-circle navdot'></i>&nbsp;&nbsp;&nbsp;";
	for (a = regions[z][0]; a <= regions[z][1]; a++)
	    navBar += pageLink(a, a, currentPage === a);
    }

    navBar += pageLink(nPages, "<i class='icon-fast-forward'></i>", false);
    navBar += "</center><br/>";

    firstBug = (currentPage - 1) * pageSize;

    // Make a bug list
    ids = "";
    for (i = firstBug; i < bugs.length && i < firstBug + pageSize; i++) {
	if (ids.length > 0)
	    ids += ",";
	ids += bugs[i];
    }

    $.ajax({
	url: "data.jsp?get=bugssummaries&for=" + ids,
	async: false,
	context: document.body
    }).done(function(data) {
	json = data;
    });

    if (json === "Not found")
    {
	$("#main").html("No bugs found");
    }

    newbugs = JSON.parse(json).bugs;
    table = "<table width='100%'><tr class='titlerow'><td style='width:28px;'>#</td><td style='width:28px;'>@</td><td>Pri</td><td>Summary</td></tr>";
    for (j = firstBug; j < bugs.length && j < firstBug + pageSize; j++) {
	for (i = 0; i !== newbugs.length; i++)
	{
	    if (newbugs[i].BUG_ID === bugs[j])
	    {
		table += getBugSummaryRow(j + firstBug + 1, newbugs[i], ((j % 2) === 0) ? "buglight" : "bugdark");
		break;
	    }
	}

    }
    table += "</table>";
    $("#main").html(navBar + table);
}

function getBugSummaryRow(num, bug, color)
{
    priority = ["High", "Medium", "Low"];
    st = ["<i class='icon-circle-blank'></i>",
	"<i class='icon-play'></i>",
	"<i class='icon-pause'></i>",
	"<i class='icon-ok'></i>"
    ];

    if (color === undefined)
	color = "white";
    row = "<tr class='bugsummaryrow " + color + "'>" +
	    "<td><b>" + num + "</b></td>" +
	    "<td>" + getUserGravatarImg(bug.ASSIGNED_TO) + "</td>" +
	    "<td>" + st[parseInt(bug.STATUS)] + " " + priority[parseInt(bug.PRIORITY)] + "</td>" +
	    "<td class='bugsummarydesctd' style='max-width:" + (winW - 170) + "px'><b><a href='#do=bugdetails&bugid=" + bug.BUG_ID + "'>" + bug.TITLE + "</a></b><span class='summarydesc'> - " + bug.DESCRIPTION + "</span></td>";

    row += "</tr>";
    return row;
}

function showBugDetails()
{
    $.ajax({
	url: "data.jsp?get=bug&for=" + bugId,
	async: false,
	context: document.body
    }).done(function(data) {
	bug = JSON.parse(data);
    });

    html = "<div style='float:right;'><a href='#do=bugedit&bugid=" + bug.BUG_ID + "' style='text-decoration:none;'><i class='icon-edit' style='font-size:3em;color:#aaa;'></i></a></div>";
    html += "<table>";
    html += "<tr><td width='92px'>" + getUserGravatarImg(bug.ASSIGNED_TO, 64) + "</td>";
    html += "<td><h1>" + bug.TITLE + "</h1>";
    switch (bug.PRIORITY)
    {
	case "0":
	    html += "<i class='icon-arrow-up' style='color:#FF4040;' title='High'>";
	    break;
	case "1":
	    html += "<i class='icon-icon-minus' style='color:#00FF00;' title='Medium'>";
	    break;
	case "2":
	    html += "<i class='icon-arrow-down' style='color:#4040FF;' title='Low'>";
	    break;
    }
    
    html += "</i>";
    
    st = ["<i class='icon-circle-blank' title='Open'></i>",
	"<i class='icon-play' title='In progress'></i>",
	"<i class='icon-pause' title='Paused'></i>",
	"<i class='icon-ok' title='Completed'></i>"
    ];

    easiness=["Easy","Medium","Hard"];
    
    html+=st[parseInt(bug.STATUS)];
    html+=" "+easiness[parseInt(bug.EASINESS)];
    html+=" <a href=''>"+bug.PRODUCT+"</a> <a href=''>"+bug.COMPONENT+"</a>";
    html+="</td></tr></table><br/>";
    html += "<span class='bugdescription'><pre>" + bug.DESCRIPTION + "</pre></span>";
    $("#main").html(html);
}

function validateBugFields()
{

}

function bugEdit()
{
    if (bugId !== "new")
    {
	$.ajax({
	    url: "data.jsp?get=bug&for=" + bugId,
	    async: false,
	    context: document.body
	}).done(function(data) {
	    bug = JSON.parse(data);
	});
    }
    else
    {
	bug = new Object();
	bugId = "new";
	bug.BUG_ID = "new";
	bug.REPORTER = jBugUser;
	bug.TITLE = "";
	bug.ASSIGNED_TO = "";
	bug.DESCRIPTION = "";
	bug.PRODUCT = "";
	bug.COMPONENT = "";
	bug.VERSION = "";
	bug.PRIORITY = "";
	bug.TARGET_MILESTONE = "";
	bug.STATUS = "0";
	bug.EASINESS = "";

    }

    html = "<table>";
    html += "<tr><td class='fieldname'>Bug ID:</td><td><input type='text' class='txtfield' id='bugid' value='" + bugId + "' readonly/></td></tr>";
    html += "<tr><td class='fieldname'>Title:</td><td><input type='text' class='txtfield' name='title' id='title' value='" + bug.TITLE + "'/></td></tr>";
    html += "<tr><td class='fieldname' valign='top'>Description:</td><td><textarea rows='5' class='txtarea' name='description' id='description'>" + bug.DESCRIPTION + "</textarea></td></tr>";
    html += "<tr><td class='fieldname'>Reporter:</td><td><input type='text' class='txtfield' name='reporter' id='reporter' value='" + bug.REPORTER + "'/></td></tr>";
    html += "<tr><td class='fieldname'>Assigned to:</td><td><input type='text' class='txtfield' name='assigned_to' id='assigned_to' value='" + bug.ASSIGNED_TO + "'/></td></tr>";
    html += "<tr><td class='fieldname'>Easiness:</td><td>\n\
    <input type='radio' name='easiness' value='0' " + ((bug.EASINESS === "0") ? "checked" : "") + ">Easy\n\
    <input type='radio' name='easiness' value='1' " + ((bug.EASINESS === "1") ? "checked" : "") + ">Medium\n\
    <input type='radio' name='easiness' value='2' " + ((bug.EASINESS === "2") ? "checked" : "") + ">Hard\n\
    </td></tr>";
    html += "<tr><td class='fieldname'>Priority:</td><td>\n\
    <input type='radio' name='priority' value='0' " + ((bug.PRIORITY === "0") ? "checked" : "") + ">High\n\
    <input type='radio' name='priority' value='1' " + ((bug.PRIORITY === "1") ? "checked" : "") + ">Medium\n\
    <input type='radio' name='priority' value='2' " + ((bug.PRIORITY === "2") ? "checked" : "") + ">Low\n\
    </td></tr>";
    html += "<tr><td class='fieldname'>Status:</td><td>\n\
    <input type='radio' name='status' value='0' " + ((bug.STATUS === "0") ? "checked" : "") + ">Open\n\
    <input type='radio' name='status' value='1' " + ((bug.STATUS === "1") ? "checked" : "") + ">In progress\n\
    <input type='radio' name='status' value='2' " + ((bug.STATUS === "2") ? "checked" : "") + ">Paused\n\
    <input type='radio' name='status' value='3' " + ((bug.STATUS === "3") ? "checked" : "") + ">Closed\
    </td></tr>";
    html += "<tr><td class='fieldname'>Product:</td><td>\n\
    <input type='text' class='txtfield' id='product' name='product' id='product' value='" + bug.PRODUCT + "'>\n\
    </td></tr>";
    html += "<tr><td class='fieldname'>Component:</td><td>\n\
    <input type='text' class='txtfield' id='component' name='component' id='component' value='" + bug.COMPONENT + "'>\n\
    </td></tr>";
    html += "<tr><td class='fieldname'>Version:</td><td>\n\
    <input type='text' class='txtfield' id='version' name='version' id='version' value='" + bug.VERSION + "'>\n\
    </td></tr>";
    html += "<tr><td class='fieldname'>Target milestone:</td><td>\n\
    <input type='text' class='txtfield' id='target_milestone' name='target_milestone' id='target_milestone' value='" + bug.TARGET_MILESTONE + "'>\n\
    </td></tr>";

    html += "<tr><td class='fieldname' colspan='2'><center><a href='javascript:saveBug()' class='btn btn-blue'>Save</a></center></td></tr>";

    html += "</table>";

    $("#main").html(html);

    // If the bug is new, set status to assigned and set the input to read only
    if (bugId === 'new')
    {
	$('input:radio[name="status",value="2"]').attr('checked', 'checked');
    }

    // Autocomplete the reporters and the assigned_to
    $.ajax({
	url: "data.jsp?get=users",
	async: true,
	context: document.body
    }).done(function(data) {

	if (data === undefined || data.indexOf(jBugUser) === -1)
	{
	    if (data !== undefined && data.length > 0)
		data += ",";
	    data += "'" + jBugUser + "'";
	}

	if (data !== undefined && data.length > 0)
	{
	    eval("var ausers=[" + data + "];");
	    $("#reporter").autocomplete({
		source: ausers
	    });
	    $("#assigned_to").autocomplete({
		source: ausers
	    });
	}
    });

    // Autocomplete the products
    $.ajax({
	url: "data.jsp?get=products",
	async: true,
	context: document.body
    }).done(function(data) {
	if (data !== undefined && data.length > 0)
	{
	    eval("var aproducts=[" + data + "];");
	    $("#product").autocomplete({
		source: aproducts
	    });
	}
    });

    // Autocomplete the component
    $.ajax({
	url: "data.jsp?get=components",
	async: true,
	context: document.body
    }).done(function(data) {
	if (data !== undefined && data.length > 0)
	{
	    eval("var acomponents=[" + data + "];");
	    $("#component").autocomplete({
		source: acomponents
	    });
	}
    });

    // Autocomplete the versions
    $.ajax({
	url: "data.jsp?get=versions",
	async: true,
	context: document.body
    }).done(function(data) {
	if (data !== undefined && data.length > 0)
	{
	    eval("var aversions=[" + data + "];");
	    $("#version").autocomplete({
		source: aversions
	    });
	}
    });

    // Autocomplete the target_milestone
    $.ajax({
	url: "data.jsp?get=target_milestones",
	async: true,
	context: document.body
    }).done(function(data) {
	if (data !== undefined && data.length > 0)
	{
	    eval("var atarget_milestone=[" + data + "];");
	    $("#target_milestone").autocomplete({
		source: atarget_milestone
	    });
	}
    });
}

function getField(fieldname)
{
    return fieldname + "=" + encodeURI($("#" + fieldname).val());
}

function saveBug()
{
    url = "data.jsp?get=updatebug";
    url += "&" + getField("bugid");
    url += "&" + getField("title");
    url += "&" + getField("description");
    url += "&" + getField("reporter");
    url += "&" + getField("assigned_to");
    easiness = "0";
    if ($('input:radio[name=easiness]:checked').val() !== undefined)
	easiness = $('input:radio[name=easiness]:checked').val();
    url += "&easiness=" + easiness;
    priority = "0";
    if ($('input:radio[name=priority]:checked').val() !== undefined)
	priority = $('input:radio[name=priority]:checked').val();
    url += "&priority=" + priority;
    status = "0";
    if ($('input:radio[name=status]:checked').val() !== undefined)
	status = $('input:radio[name=status]:checked').val();
    url += "&status=" + status;
    url += "&" + getField("product");
    url += "&" + getField("component");
    url += "&" + getField("version");
    url += "&" + getField("target_milestone");

    // Save the bug 
    $.ajax({
	url: url,
	async: true,
	context: document.body
    }).done(function(data) {
	updateBugStatusBar();
	updateOpenBugsList();
	window.location = "#do=bugdetails&bugid=" + data;
    });
}