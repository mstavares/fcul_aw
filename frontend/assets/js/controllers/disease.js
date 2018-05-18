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
				articles += "<p><b>" + content.articles[i].title + "</b><br />";
				articles += "<p>" + content.articles[i].description + "</p>";
			}
			
			for (var i = 0; i < content.images.length; i++){
				var img = document.createElement("img");
				img.setAttribute('src', content.images[i].url);
				img.setAttribute('class', 'mySlides');
				document.getElementById("slide-container").appendChild(img);
			}

			for (var i = 0; i < content.tweets.length; i++){
				tweets += " " + content.tweets[i].description + "<br />";
			}

			document.getElementById('disease').innerHTML = info;
			document.getElementById('articles').innerHTML = articles;
			document.getElementById('tweets').innerHTML = tweets;
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get/" + diseaseId, true);
	ajax.send();
	
}