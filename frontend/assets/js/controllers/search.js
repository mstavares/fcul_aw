var hash = window.location.hash.substr(1);
var searchTerm = hash.split('=')[1];

if (searchTerm != undefined){

	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
			var content = JSON.parse(html);
			var diseases = "<ul>";

			for (var i = 0; i < content.lenght; i++){
				diseases += "<li><a href='#disease=" + content[i].id + "'>" + content[i].name + "</a></li>";
			}

			diseases += "</ul>";
			
			document.getElementById('search-results').innerHTML = diseases;
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get_by_name_fragment/" + searchTerm, true);
	ajax.send();
	
}