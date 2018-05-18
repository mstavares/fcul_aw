// Automatic Slideshow - change image every 3 seconds
var myIndex = 0;
carousel();

function carousel() {
var i;
var x = document.getElementsByClassName("mySlides");
for (i = 0; i < x.length; i++) {
	x[i].style.display = "none";
}
myIndex++;
if (myIndex > x.length) {myIndex = 1}
x[myIndex-1].style.display = "block";
setTimeout(carousel, 3000);
}

setPage(document.getElementById("diseases-menu"));

var hash = window.location.hash.substr(1);
var diseaseId = hash.split('=')[1];

if (diseaseId != undefined){

	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
			document.getElementById("search-input").value="";
			document.getElementById("livesearch").style.visibility = "hidden"; 

			var content = JSON.parse(html);

			document.getElementById('disease-name').innerHTML = "Disease: " + content.name;

			var info = "";
			var articles = "";
			var tweets = "";

			info += "<b>Database id:</b> " + content.id + "<br />";
			info += "<b>Disease description:</b> " + content.description + "<br />";
			info += "<b>Wikipedia link:</b> " + content.derivedFrom + "<br />";
			if(content.field !== null)
				info += "<b>Field:</b> " + content.field + "<br />";
			if(content.dead !== null)
				info += "<b>People killed by this disease:</b> " + content.dead + "<br />";

			for (var i = 0; i < content.articles.length; i++){
				articles += "<div class='pubmed-container'>"
					articles += "<div class='pubmed-feedback' onclick='updatePubMedFeedback()'></div>"
					articles += "<div class='pubmed-title'><a href='https://www.ncbi.nlm.nih.gov/pubmed/" + content.articles[i].pubMedId + "'>" + content.articles[i].title + "</a></div>"
					articles += "<div class='pubmed-description'>" + content.articles[i].description + "</div>"
					articles += "<div class='pubmed-related'><b>Related diseases:</b> "; 
					for (var j = 0; j < content.articles[i].mentionedDiseases.length; j++){
						articles+= "<a href='#disease=" + content.articles[i].mentionedDiseases[j].id + "'>" + content.articles[i].mentionedDiseases[j].name + ", </a>"
					}
					articles+= "</div>";

				articles += "</div>";
			}
			
			for (var i = 0; i < content.images.length; i++){
				var img = document.createElement("img");
				img.setAttribute('src', content.images[i].url);
				img.setAttribute('class', 'mySlides');
				document.getElementById("slide-container").appendChild(img);
			}

			for (var i = 0; i < content.tweets.length; i++){
				tweets += "<div class='tweet-container'>"
					tweets += "<div class='tweet-description'>" + content.tweets[i].description + "</div>"
					tweets += "<div class='tweet-date'>" + content.tweets[i].pubDate + "</div>"
				tweets += "</div>";
			}

			document.getElementById('disease').innerHTML = info;
			document.getElementById('articles').innerHTML = articles;
			document.getElementById('tweets').innerHTML = tweets;
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get/" + diseaseId, true);
	ajax.send();
	
}