window.junitui = {};
var progressStyles = ".progress {\n"
        + "  width: 100%;\n"
        + "  height: 50px;\n"
        + "}\n"
        + ".progress-wrap {\n"
        + "  background: #f80;\n"
        + "  margin: 20px 0;\n"
        + "  overflow: hidden;\n"
        + "  position: relative;\n"
        + "  .progress-bar {\n"
        + "    background: #ddd;\n"
        + "    left: 0;\n"
        + "    position: absolute;\n"
        + "    top: 0;\n"
        + "  }\n"
        + "}" +
        "/* This sets the dimensions of the Progress Bar element */\n" +
        "progress {\n" +
        "  width: 300px;\n" +
        "  height: 17px;\n" +
        "        display: block;\n" +
        "        /* turn off defaults */\n" +
        "        -webkit-appearance: none;\n" +
        "        border: none;\n" +
        "}\n" +
        " \n" +
        "/* This rule sets the appearance of the progress bar background */\n" +
        "progress::-webkit-progress-bar {\n" +
        "        background: black;\n" +
        "        border-radius: 50px;\n" +
        "        padding: 2px;\n" +
        "        box-shadow: 0 1px 0px 0 rgba(255, 255, 255, 0.2);\n" +
        "}\n" +
        " \n" +
        "/* This rule sets the appearance of the progress bar meter */\n" +
        "progress::-webkit-progress-value {\n" +
        "    border-radius: 50px;\n" +
        "          box-shadow: inset 0 1px 1px 0 rgba(255, 255, 255, 0.4);  \n" +
        "    background-size: 30px 30px;\n" +
        "    background-image: linear-gradient(135deg, rgba(255, 255, 255, .15) 25%, transparent 25%,\n" +
        "                      transparent 50%, rgba(255, 255, 255, .15) 50%, rgba(255, 255, 255, .15) 75%,\n" +
        "                      transparent 75%, transparent);   \n" +
        "                    \n" +
        "}";

head = document.head || document.getElementsByTagName('head')[0],
style = document.createElement('style');

style.type = 'text/css';
if (style.styleSheet) {
    style.styleSheet.cssText = progressStyles;
} else {
    style.appendChild(document.createTextNode(progressStyles));
}
head.appendChild(style);

window.junitui.setImageURLs = function (doc, toggleSmallExpand, folderHorizontal, toggleSmall) {
    var treeStyles = "h1:not(#test-header) { display: none; }\n"+ 
            "ul {display: none; }" +
            "ol.tree\n" +
            "{\n" +
            "	padding: 0 0 0 30px;\n" +
            "	width: 100%;\n" +
            "}\n" +
            "	li \n" +
            "	{ \n" +
            "		position: relative; \n" +
            "		margin-left: -15px;\n" +
            "		list-style: none;\n" +
            "	}\n" +
            "	li.file\n" +
            "	{\n" +
            "		margin-left: -1px !important;\n" +
            "	}\n" +
            "		li.file a\n" +
            "		{\n" +
            "			background: url(" + doc + ") 0 0 no-repeat;\n" +
            "			color: #4C4C4C;\n" +
            "			padding-left: 21px;\n" +
            "			text-decoration: none;\n" +
            "			display: block;\n" +
            "		}\n" +
            "	li input\n" +
            "	{\n" +
            "		position: absolute;\n" +
            "		left: 0;\n" +
            "		margin-left: 0;\n" +
            "		opacity: 0;\n" +
            "		z-index: 2;\n" +
            "		cursor: pointer;\n" +
            "		height: 1em;\n" +
            "		width: 1em;\n" +
            "		top: 0;\n" +
            "	}\n" +
            "		li input + ol\n" +
            "		{\n" +
            "			background: url(" + toggleSmallExpand + ") 40px 0 no-repeat;\n" +
            "			margin: -0.938em 0 0 -44px; /* 15px */\n" +
            "			height: 1em;\n" +
            "		}\n" +
            "		li input + ol > li { display: none; margin-left: -14px !important; padding-left: 1px; }\n" +
            "	li label\n" +
            "	{\n" +
            "		background: url(" + folderHorizontal + ") 15px 1px no-repeat;\n" +
            "		cursor: pointer;\n" +
            "		display: block;\n" +
            "		padding-left: 37px;\n" +
            "	}\n" +
            "\n" +
            "	li input:checked + ol\n" +
            "	{\n" +
            "		background: url(" + toggleSmall + ") 40px 5px no-repeat;\n" +
            "		margin: -1.25em 0 0 -44px; /* 20px */\n" +
            "		padding: 1.563em 0 0 80px;\n" +
            "		height: auto;\n" +
            "	}\n" +
            "		li input:checked + ol > li { display: block; margin: 0 0 0.125em;  /* 2px */}\n" +
            "		li input:checked + ol > li:last-child { margin: 0 0 0.063em; /* 1px */ }"
            ;
    $("head style").append(document.createTextNode(treeStyles));
    $("body").prepend($("<h1 id='test-header'>DukeScript Test Runner</h1>"));

};


