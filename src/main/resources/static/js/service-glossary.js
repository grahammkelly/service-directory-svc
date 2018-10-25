var md;

var fromMarkdownOrString = function(str) {
    if (md != null && str.indexOf("\n") > -1) {
        return md.makeHtml(str);
    }
    return "<p>" + str + "</p>";
};

var currentHostUrl = function () {
    return window.location.protocol + "//" + window.location.host;
};

var currentUrl = function () {
    return currentHostUrl() + window.location.pathname;
};

var queryParameters = function () {
    var fullUrl = String(window.location);
    var qmarklocaton = fullUrl.indexOf('?');

    if (qmarklocaton > -1) {
        return fullUrl.substring(qmarklocaton);
    }
    return "";
};

var desiredProject = function () {
    var queryParams = queryParameters();
    var start = queryParams.indexOf("project=");
    if (start > -1) {
        var p = queryParams.substring(start+8);
        var end = p.indexOf('&');
        if (end > -1) {
            return p.substring(0, end);
        }
        return p;
    }
    return "";
};

var removeChildren = function (element) {
    if (element) {
        element.innerHTML = '';    //May be slow, in which case loop through. I think this depends on the contents, as we're only using text here, setting the HTML should be OK
        //In case slow:
        // while (element.firstChild) {
        //     element.removeChild(projectLinks.firstChild);
        // }
    }
};

var createElement = function(type, text) {
    var element = document.createElement(type);
    if (text != null) {
        element.innerText = text;
    }
    return element;
};

var createListItemForProject = function(project) {
    var createLinkForProject = function(project) {
        var linkElement = document.createElement("a");
        linkElement.innerHTML = project;
        linkElement.setAttribute("href", currentUrl() + "?project=" + project);
        return linkElement;
    };

    var li = document.createElement("li");
    li.appendChild(createLinkForProject(project));
    return li;
};

var createProjectLinkMenu = function (projectList) {
    var projectLinks = document.getElementById("project-links");
    removeChildren(projectLinks);

    var list = document.createElement("ul");

    var key;
    for (key in projectList) {
        if (projectList.hasOwnProperty(key)) {
            var linkElement = createListItemForProject(key);
            list.appendChild(linkElement)
        }
    }
    projectLinks.appendChild(list);
};

var loadProjectsAndCreateNavMenu = function () {
    $.ajax({
        url: currentHostUrl() + "/api/projects",
    }).then(function (data) {
        createProjectLinkMenu(data);
    });
};

var displayErrorDetails = function(projectBody, projectToLoad, xhr) {
    projectBody.appendChild(createElement("p", xhr.status + ": Project " + projectToLoad + " does not exist"));
};

var linkExists = function(url) {
    console.log("Checking if " + url + " exists");

    var exists;

//     var request = false;
//     if (window.XMLHttpRequest) {
//         request = new XMLHttpRequest;
//     } else if (window.ActiveXObject) {
//         request = new ActiveXObject("Microsoft.XMLHttp");
//     }
//
//     if (request) {
//         request.open("GET", url);
//         if (request.status < 400) {
//             console.log("YES, it does");
//             exists = true;
//         }
//     }
//     console.log("NOPE");
//     exists = false;

    var jqXHR = $.get({
        url: url,
        async: false
    }).done(function () {
        exists = true;
    }).fail(function () {
        console.log("request: " + JSON.stringify(jqXHR));
        if (jqXHR.status == 404) {
            console.log("NOPE");
            exists = false;
        } else {
            console.log("I think so");
            exists = true;
        }
    });

    return exists;
};

var displayProjectDetails = function(projectBody, projectInfo) {
    //This whole method is shite. I don't like the idea of manipulating HTML as text
    // Needs refactor

    var addInnerElement = function(innerElement, outerElement) {
        if (outerElement != null && innerElement != null) {
            outerElement.appendChild(innerElement);
        }
    };

    var addDivToProjectDetails = function(div) {
        addInnerElement(div, projectBody);
    };

    var createDiv = function(divId) {
        var newDiv = document.createElement("div");
        if (divId != null) {
            newDiv.setAttribute("id", String(divId));
        }
        return newDiv;
    };

    var createListEntryWithName = function(name, link) {
        var linkWithName = function(name, link) {
            var linkElement = document.createElement("a");
            linkElement.innerHTML = name;
            linkElement.setAttribute("href", link);
            return linkElement;
        };

        var li = document.createElement("li");
        li.appendChild(linkWithName(name, link));
        return li;
    };

    var displayProjectBadges = function(outerDiv, projectName) {
        var jobStatusDiv = document.createElement("div");
        jobStatusDiv.setAttribute("id", "project-badges");
        outerDiv.appendChild(jobStatusDiv);

        var badges = {};

        var jenkinsJob = model.jenkinsAddress + "/job/" + projectName + "/job/master/";
        badges[jenkinsJob] = {
            icon: model.jenkinsAddress + "/buildStatus/icon?job=" + projectName + "/master/",
            altText: "Build not generated via CI process"
        };

        var sonarLink = model.sonarHost + "/overview?id=" + encodeURIComponent(model.sonarProjectKey);
        badges[sonarLink] = {
            icon: model.sonarHost + "/api/badges/measure?key=" + encodeURIComponent(model.sonarProjectKey) + "&metric=coverage",
            altText: "Need to install the Sonar badges plugin"
        };

        var link;
        for (link in badges) {
            console.log(link + ": " + JSON.stringify(badges[link]));
            if (badges.hasOwnProperty(link)) {
                var badge = badges[link];
                jobStatusDiv.innerHTML +=
                    "<p><a href=\"" + link + "\"><img src=\"" + badge.icon + "\" alt=\"" + String(badge.altText) + "\"/></a></p>";
            }
        }
    };

    var displayProjectTestCoverage = function(outerDiv) {
        // if (model.sonarCoverage !== null) {
        //     // console.log("Sonar coverage: " + model.sonarCoverage);
        //
        //     var testCoverageDiv = document.createElement("div");
        //     testCoverageDiv.setAttribute("id", "project-test-coverage");
        //     outerDiv.appendChild(testCoverageDiv);
        //
        //     var sonarLink = model.sonarHost + "/overview?id=" + encodeURIComponent(model.sonarProjectKey);
        //     testCoverageDiv.innerHTML =
        //         "<p>Test coverage: <a href=\"" + sonarLink + "\">" + model.sonarCoverage + " %</a></p>";
        // }
    };

    var displayProjectDescription = function(outerDiv) {
        var descDiv = document.createElement("div");
        descDiv.setAttribute("id", "project-description");
        outerDiv.appendChild(descDiv);

        descDiv.innerHTML = "<h2>Description</h2>";
        descDiv.innerHTML += fromMarkdownOrString(projectInfo.desc);
    };

    //This makes use of markdown for the project description. Be warned
    // Markdown is totally open to XSS from the data being converted!
    var getBasicProjectInfo = function() {
        var basicInfoDiv = createDiv("project-basic-info");

        basicInfoDiv.innerHTML = "<h1>" + projectInfo.displayName + "</h1>";
        displayProjectBadges(basicInfoDiv, projectInfo.name);
        displayProjectTestCoverage(basicInfoDiv);
        displayProjectDescription(basicInfoDiv);


        return basicInfoDiv;
    };

    var getOwnershipInfo = function() {
        var owningTeam = projectInfo.owner;
        var contact = owningTeam.contact;

        var ownerDiv = createDiv("project-owner-info");
        ownerDiv.innerHTML = "<h2>Contact</h2>";

        if (owningTeam.hasOwnProperty("name") && owningTeam.name != null) {
            ownerDiv.innerHTML = "<p>" + owningTeam.name + "</p>";
        }

        if (contact.hasOwnProperty("slack") && contact.slack != null) {
            ownerDiv.innerHTML += "" +
                "<p>" +
                    "<b>Slack</b> - <a href=\"https://travelportdigital.slack.com/app_redirect?channel=" + contact.slack + "\">" + contact.slack + "</a>" +
                "</p>";
        }

        if (contact.hasOwnProperty("msteams") && contact.msteams != null) {
            var msteams = contact.msteams;
            ownerDiv.innerHTML += "" +
                "<p>" +
                "<b>MS Teams channel</b> - <a href=\"" + msteams.link + "\">" + msteams.name + "</a>" +
                "</p>";
        }

        ownerDiv.innerHTML += "" +
            "<p>" +
                "<b>Email</b> - <a href=\"mailto:" + contact.email + "\">" + contact.email + "</a>" +
            "</p>";

        if (owningTeam.hasOwnProperty("members") && owningTeam.members != null) {
            ownerDiv.innerHTML += "<h2>Team members</h2>"

            var memberList = document.createElement("ul");

            var teamMembers = owningTeam.members;
            var member;
            //Assumes structure of 'members' is Map of person -> email address
            for (member in teamMembers) {
                if (teamMembers.hasOwnProperty(member)) {
                    memberList.appendChild(createListEntryWithName(member, "mailto:" + teamMembers[member]));
                }
            }
        }

        return ownerDiv;
    };

    var getTechnicalInfo = function() {
        var techInfoDiv = createDiv("project-technical-info");
        var httpRepo = "https://" + model.gitHost + "/" + projectInfo.name;
        techInfoDiv.innerHTML = "<h1>Technical information</h1>" +
            "<p>Project README <a href=\"" + httpRepo + "/src/master/README.md\">" + httpRepo + "/src/master/README.md</a></p>" +
            "<p>Clone on git <a href=\"https://" + model.gitHost + "/" + projectInfo.name + "\">git clone git@" + model.gitHost + "/" + projectInfo.name + ".git</a></p>" +
            "<p>CI Build job <a href=\"" + model.jenkinsAddress + "/job/" + projectInfo.name + "\">" + model.jenkinsAddress + "/job/" + projectInfo.name + "</a></p>";
        return techInfoDiv;
    };

    var getOperationalInfo = function() {
        var opsInfoDiv = createDiv("project-operational-info");
        //Does nothing
        return null;
    };

    var getRelatedInfo = function() {

        var dependencies = function(divId, heading, dependencies, createTheLink) {
            var relatedProjectsDiv = createDiv(divId);

            // console.log("Dependencies [" + divId + "]: (size: " + (dependencies==null?0:dependencies.length) + ") " + JSON.stringify(dependencies, null, 4))
            if (dependencies != null && dependencies.length > 0) {
                var hdr = document.createElement("h2");
                hdr.innerText = heading;
                relatedProjectsDiv.appendChild(hdr);

                var list = document.createElement("ul");

                var linkName;
                for (linkName in dependencies) {
                    if (dependencies.hasOwnProperty(linkName)) {
                        list.appendChild(createTheLink(linkName));
                    }
                }

                var para = document.createElement("p");
                para.appendChild(list);
                relatedProjectsDiv.appendChild(para);
            }
            return relatedProjectsDiv;
        };

        var upstreamDependencies = function() {
            return dependencies(
                "upstream-dependencies",
                "Depended upon by",
                projectInfo.related.dependencyOf,
                function(linkNum){
                    return createListItemForProject(projectInfo.related.dependencyOf[linkNum].name);
                }
            );
        };

        var downstreamDependencies = function() {
            return dependencies(
                "downstream-dependencies",
                "Depends on",
                projectInfo.related.dependsUpon,
                function(linkNum){
                    return createListItemForProject(projectInfo.related.dependsUpon[linkNum].name);
                }
            );
        };

        var linkedPages = function() {
            return dependencies(
                "linked-pages",
                "Links",
                projectInfo.related.links,
                function(linkNum) {
                    return createListEntryWithName(
                        projectInfo.related.links[linkNum].name,
                        projectInfo.related.links[linkNum].link
                    );
                }
            );
        };

        var relatedInfoDiv = createDiv("project-related-info");
        relatedInfoDiv.innerHTML = "<h1>Useful Information</h1>";

        addInnerElement(upstreamDependencies(), relatedInfoDiv);
        addInnerElement(downstreamDependencies(), relatedInfoDiv);
        addInnerElement(linkedPages(), relatedInfoDiv);

        //Does nothing
        return relatedInfoDiv;
    };

    // console.log("Project Info: " + JSON.stringify(projectInfo, null, 4));

    addDivToProjectDetails(getBasicProjectInfo());
    addDivToProjectDetails(getOwnershipInfo());
    addDivToProjectDetails(getTechnicalInfo());
    addDivToProjectDetails(getOperationalInfo());
    addDivToProjectDetails(getRelatedInfo());

    // addDivToProjectDetails(createElement("p", "More project details for " + projectInfo.name + " will appear here"));
};

var loadProjectDetails = function (projectBody, projectToLoad) {
    $.ajax({
        url: currentHostUrl() + "/api/project/" + projectToLoad,
        success: function (projectInfo) {
            displayProjectDetails(projectBody, projectInfo);
        },
        error: function(xhr, ajaxOptions, thrownError) {
            displayErrorDetails(projectBody, projectToLoad, xhr);
        }
    });
};

var displayProject = function () {
    var setProjectHeading = function(heading) {
        projectHeading.appendChild(createElement("h1", heading));
    };

    var defaultProjectContents = function () {
        setProjectHeading(model.pageTitle);
        projectBody.appendChild(createElement("p", "Project details will appear here"));
    };

    var project = desiredProject();

    var projectHeading = document.getElementById("project-name");
    removeChildren(projectHeading);

    var projectBody = document.getElementById("project-body");
    removeChildren(projectBody);

    if (project === "") {
        defaultProjectContents();
    } else {
        setProjectHeading(project);
        loadProjectDetails(projectBody, project);
    }
};

if (showdown != null) {
    md = new showdown.Converter({
        omitExtraWLInCodeBlocks: true,
        headerLevelStart: 3,
        simplifiedAutoLink: true,
        tables: true,
        ghCodeBlocks: true,
        tasklists: true
    });
    md.setFlavor('github');
}

$(document).ready(function () {
    loadProjectsAndCreateNavMenu();
    displayProject();
});
