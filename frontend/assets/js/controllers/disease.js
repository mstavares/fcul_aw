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
			var content = JSON.parse(html);

			document.getElementById('disease-name').innerHTML = "Disease: " + content.name;

			var info = "";

			info += "Disease id: " + content.id + "<br />";
			info += "Disease name: " + content.name + "<br />";
			info += "Disease description: " + content.description + "<br />";
			info += "Wikipedia: " + content.derivedFrom + "<br />";
			info += "Field: " + content.field + "<br />";
			info += "Dead: " + content.dead + "<br />";

			for (var i = 0; i < content.articles.length; i++){
				info += "Artigo " + i + ": " + content.articles[i].title + "<br />";
			}
			
			for (var i = 0; i < content.images.length; i++){
				info += "Imagem " + i + ": " + content.images[i].url + "<br />";
			}

			for (var i = 0; i < content.tweets.length; i++){
				info += "Tweet " + i + ": " + content.tweets[i].url + "<br />";
			}

			document.getElementById('disease').innerHTML = info;
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get/" + diseaseId, true);
	ajax.send();
	
}