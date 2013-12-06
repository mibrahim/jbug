<nav class="navbar navbar-default" role="navigation">
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#">Brand</a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav">
            <li><a href="#" title="Home"><i class="glyphicon glyphicon-home"></i></a></li>
            <li><a href="#do=bugedit&bugid=new" onclick="showCurrentPage()" title="New bug"><i class="glyphicon glyphicon-star"></i></a></li>
            <li><a href="#do=roadmap" title="New bug"><i class="glyphicon glyphicon-road"></i></a></li>

            <li><a href="#">Link</a></li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Dropdown <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="#">Action</a></li>
                    <li><a href="#">Another action</a></li>
                    <li><a href="#">Something else here</a></li>
                    <li class="divider"></li>
                    <li><a href="#">Separated link</a></li>
                    <li class="divider"></li>
                    <li><a href="#">One more separated link</a></li>
                </ul>
            </li>
        </ul>
        <form class="navbar-form navbar-left" role="search">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Search">
            </div>
            <button type="submit" class="btn btn-default">Submit</button>
        </form>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="#">Link</a></li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Dropdown <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="#">Action</a></li>
                    <li><a href="#">Another action</a></li>
                    <li><a href="#">Something else here</a></li>
                    <li class="divider"></li>
                    <li><a href="#">Separated link</a></li>
                </ul>
            </li>
        </ul>
    </div><!-- /.navbar-collapse -->
</nav>

<div style="width:100%">
    <div  class="collapse navbar-collapse">
        <div class="nav navbar-nav">
            <span class="status" id="bugstatus">
                Open Bugs: <span id="openbugs"></span>
            </span>
        </div>
        <form class="navbar-form navbar-left" role="search">
            <div class="form-group">
                <input type="text" id="searchbar" name="searchbar" class="form-control" placeholder="SEARCH..."/>
            </div>
        </form>
        <a href="javascript:search()" class="btn btn-primary">&Gt;</a>
        <div class="navbar-right">
            <a id="grv" href="javascript:getUserEmail();"></a>
            <a href="" class="btn btn-primary" title="Search"><i class="glyphicon glyphicon-search"></i></a>
        </div>
    </div>
</div>