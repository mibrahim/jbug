
<div class="navbar navbar-default" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#do=home"><i class="glyphicon glyphicon-home"></i> jBug</a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav">
            <li><a href="#do=bugedit&bugid=new" onclick="showCurrentPage()" title="New issue"><i class="glyphicon glyphicon-star"></i> New issue</a></li>
            <li><a href="#do=roadmap" title="New bug"><i class="glyphicon glyphicon-road"></i> Road map</a></li>
            <li><a><b>Open Bugs:</b> <span id="openbugs" class="badge"></span></a></li>
            <li><a id="grv" href="javascript:getUserEmail();"></a></li>
            <li><form onsubmit="return search();" id='searchform' class="navbar-form navbar-right" role="search">
                <div class="form-group" style="width:300px">
                    <input type="text" id="searchbar" name="searchbar" class="form-control" placeholder="Search">
                </div>
                <a id='searchbutton' href="javascript:search()" class="btn btn-primary">&Gt;</a>
            </form></li>
        </ul>
    </div><!-- /.navbar-collapse -->
</div>