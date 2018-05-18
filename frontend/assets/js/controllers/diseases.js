ajax.onreadystatechange = function () {

	var html = ajax.responseText;
	if (ajax.readyState == 4 && ajax.status == 200) {
		var content = JSON.parse(html);
		var diseases = "<ul>";
		
		for (var i = 0; i < content.length; i++){
			diseases += "<li><a href='#disease=" + content[i].id + "'>" + content[i].name + "</a></li>";
		}

		diseases += "</ul>";
		
		document.getElementById('app').innerHTML = diseases;
	}


};

ajax.open('GET', apiBaseUrl + "disease/get_all/0", true);
ajax.send();