var ajax = createObject();


function createObject(){

    var xmlHttp;

    if (window.XMLHttpRequest){

        try{
            xmlHttp = new XMLHttpRequest();
        }catch(e){
            xmlHttp = false;
        }
        
    }else if (window.ActiveXObject){
        
        try{
            xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
        }catch(e){
            xmlHttp = false;
        }

    }
    
    if (!xmlHttp)
        alert ("Something went wrong.")
    else
        return xmlHttp;  

}


function setPage(obj){

	var current = document.getElementsByClassName("active");

	console.log(current[0]);

	current[0].classList.remove("active");
	
	obj.parentElement.classList.add("active");
}


function search(e){
	var input = document.getElementById("search-input").value;

    if (e.keyCode == 13 || e == 0) {
		if (input)
			window.location.hash = "#search=" + input;
		return;
	}
	
	if (input.length==0) {
		document.getElementById("livesearch").innerHTML="";
		document.getElementById("livesearch").style.border="0px";
		return;
	}

	ajax.onreadystatechange = function () {

        var html = ajax.responseText;
        if (ajax.readyState == 4 && ajax.status == 200) {
			var content = JSON.parse(html);
			var diseases = "";

			for (var i = 0; i < content.lenght; i++){
				diseases += "<a href='#disease=" + content[i].id + "'>" + content[i].name + "</a><br />";
			}
			
			document.getElementById("livesearch").innerHTML = diseases;
			document.getElementById("livesearch").style.border="1px solid #A5ACB2";
        }


    };
    
    ajax.open('GET', apiBaseUrl + "disease/get_by_name_fragment/" + input, true);
    ajax.send();
}