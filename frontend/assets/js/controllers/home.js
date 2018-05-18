document.body.style.backgroundImage = "url('/assets/img/bckg_home.png')"; 
document.getElementById("search").classList.add("home-search");

function restorePage(){
	document.body.style.backgroundImage = "url('/assets/img/bckg.png')"; 
	document.getElementById("search").classList.remove("home-search");
	window.removeEventListener('hashchange', restorePage);
}

window.addEventListener('hashchange', restorePage);