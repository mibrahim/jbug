<nav class="navbar navbar-default" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#"><i class="glyphicon glyphicon-home"></i> jBug</a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav">
            <li><a href="#do=bugedit&bugid=new" onclick="showCurrentPage()" title="New issue"><i class="glyphicon glyphicon-star"></i> New issue</a></li>
            <li><a href="#do=roadmap" title="New bug"><i class="glyphicon glyphicon-road"></i> Road map</a></li>
            <li><a><b>Open Bugs:</b> <span id="openbugs" class="badge"></span></a></li>
            <li><a id="grv" href="javascript:getUserEmail();"></a></li>
        </ul>
        <form class="navbar-form navbar-left" role="search">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Search">
            </div>
            <button type="submit" class="btn btn-default">Submit</button>
        </form>
    </div><!-- /.navbar-collapse -->
</nav>