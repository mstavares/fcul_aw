ajax.onreadystatechange = function () {

	var html = ajax.responseText;
	if (ajax.readyState == 4 && ajax.status == 200) {
		var content = JSON.parse(html);
		var stats = "<ul>";
		
		stats += "<li>Total number of Diseases: " + content.numberOfDiseases + "</li>";
		stats += "<li>Total number of PubMeds: " + content.numberOfPubMeds + "</li>";
		stats += "<li>Total number of Tweets: " + content.numberOfTweets + "</li>";
		stats += "<li>Total number of Images: " + content.numberOfImages + "</li>";
		stats += "<li>Average Pubmeds By Disease: " + content.averagePubmedsByDisease + "</li>";
		stats += "<li>Average Tweets By Disease: " + content.averageTweetsByDisease + "</li>";
		stats += "<li>Average Images By Disease: " + content.averageImagesByDisease + "</li>";
		stats += "</ul>";
		
		document.getElementById('statistics').innerHTML = stats;
	}


};

ajax.open('GET', apiBaseUrl + "disease/get_statistics", true);
ajax.send();