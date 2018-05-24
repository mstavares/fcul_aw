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

			var diseaseId = content.id;
			var info = "";
			var articles = "";
			var tweets = "";
			var images = "";

			info += "<b>Database id:</b> " + content.id + "<br />";
			info += "<b>Disease description:</b> " + content.description + "<br />";
			info += "<b>Wikipedia link:</b> " + content.derivedFrom + "<br />";
			if(content.field !== null)
				info += "<b>Field:</b> " + content.field + "<br />";
			if(content.dead !== null)
				info += "<b>People killed by this disease:</b> " + content.dead + "<br />";

			info += "<b>Related diseases:</b> ";

			for (var i = 0; i < content.diseases.length; i++){
				info += "<a href='#disease=" + content.diseases[i].id + "'>" + content.diseases[i].name + "</a>, ";
			}

			for (var i = 0; i < content.articles.length; i++){
				var pubmedId = content.articles[i].id;
				var args = '"downPub' + pubmedId + '", ' + diseaseId + ', ' + pubmedId + ', 3';
				var ExplicitUp = '"upPub' + pubmedId + '", ' + diseaseId + ', ' + pubmedId + ', 1';
				var argsImplicit = "-1" + diseaseId + ", " + pubmedId + ", 0";
				articles += "<div class='pubmed-container'>";
					articles += "<div class='pubmed-feedback'><a id='downPub" + pubmedId + "' onclick='updatePubMedFeedback(" + args + ")'><i class='fas fa-thumbs-down'></i></a>";
					articles += "<a id='upPub" + pubmedId + "' onclick='updatePubMedFeedback(" + ExplicitUp + ")'><i class='fas fa-thumbs-up'></i></a></div>";
					articles += "<div class='pubmed-title'><a target='blank' href='https://www.ncbi.nlm.nih.gov/pubmed/" + content.articles[i].pubMedId + "' onclick='updatePubMedFeedback(" + argsImplicit + ")'>" + content.articles[i].title + "</a></div>";
					articles += "<div class='pubmed-description'>" + content.articles[i].description + "</div>";
					articles += "<div class='pubmed-related'><b>Related diseases:</b> "; 
					for (var j = 0; j < content.articles[i].mentionedDiseases.length; j++){
						articles+= "<a href='#disease=" + content.articles[i].mentionedDiseases[j].id + "'>" + content.articles[i].mentionedDiseases[j].name + ", </a>";
					}
					articles+= "</div>";

				articles += "</div>";
			}
			
			for (var i = 0; i < content.images.length; i++){
				var imgId = content.images[i].id;
				var args = '"downImg' + imgId + '", ' + diseaseId + ", " + imgId + ", 3";
				var ExplicitUp = '"upImg' + imgId + '", ' + diseaseId + ", " + imgId + ", 1";
				var argsImplicit = "-1" + diseaseId + ", " + imgId + ", 0";
				images += "<div class='mySlides'>";
					images += "<a target='blank' href='" + content.images[i].url + "' onclick='updateImgFeedback(" + argsImplicit + ")'><img src='" + content.images[i].url + "'></img></a>";
					images += "<a id='downImg" + imgId + "' onclick='updateImgFeedback(" + args + ")'><i class='fas fa-thumbs-down'></i></a>";
					images += "<a id='upImg" + imgId + "' onclick='updateImgFeedback(" + ExplicitUp + ")'><i class='fas fa-thumbs-up'></i></a>";
				images += "</div>";
			}

			for (var i = 0; i < content.tweets.length; i++){
				var tweeetId = content.tweets[i].id;
				var args = '"downTweet' + tweeetId + '", ' + diseaseId + ", " + tweeetId + ", 3";
				var ExplicitUp = '"upTweet' + tweeetId + '", ' + diseaseId + ", " + tweeetId + ", 1";
				var argsImplicit = "-1" + diseaseId + ", " + tweeetId + ", 0";
				tweets += "<div class='tweet-container'>";
					tweets += "<div class='tweet-description'>" + content.tweets[i].description + "</div>";
					tweets += "<div class='tweet-feedback'><a id='downTweet" + tweeetId + "' onclick='updateTweetFeedback(" + args + ")'><i class='fas fa-thumbs-down'></i></a>";
					tweets += "<a id='upTweet" + tweeetId + "' onclick='updateTweetFeedback(" + ExplicitUp + ")'><i class='fas fa-thumbs-up'></i></a></div>";
					tweets += "<div class='tweet-date'><a target='blank' href='https://twitter.com/statuses/" + content.tweets[i].url + "' onclick='updateTweetFeedback(" + argsImplicit + ")'>" + content.tweets[i].pubDate + "</a></div>";
				tweets += "</div><br />";
			}

			document.getElementById('disease').innerHTML = info;
			document.getElementById('articles').innerHTML = articles;
			document.getElementById('tweets').innerHTML = tweets;
			document.getElementById('slide-container').innerHTML = images;
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get/" + diseaseId, true);
	ajax.send();
	
}


function updatePubMedFeedback(feedbackName, diseaseId, pubmedId, op){
	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {

			var content = JSON.parse(html);

			document.getElementById(feedbackName).classList.add("activated");

			console.log("Feedback PubMed " + feedbackName + ": " + content);
		}

    };
    
    ajax.open('POST', apiBaseUrl + "feedback/pubmed?diseaseId=" + diseaseId + "&pubmedId=" + pubmedId + "&operation=" + op, true);
	ajax.send();
}



function updateImgFeedback(feedbackName, diseaseId, imgId, op){
	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {

			var content = JSON.parse(html);

			document.getElementById(feedbackName).classList.add("activated");

			console.log("Feedback Image " + feedbackName + ": " + content);
		}

    };
    
    ajax.open('POST', apiBaseUrl + "feedback/image?diseaseId=" + diseaseId + "&imageId=" + imgId + "&operation=" + op, true);
	ajax.send();
}


function updateTweetFeedback(feedbackName, diseaseId, tweetId, op){
	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {

			var content = JSON.parse(html);

			document.getElementById(feedbackName).classList.add("activated");

			console.log("Feedback Tweet " + feedbackName + ": " + content);
		}

	};
	
    ajax.open('POST', apiBaseUrl + "feedback/tweet?diseaseId=" + diseaseId + "&tweetId=" + tweetId + "&operation=" + op, true);
	ajax.send();
}