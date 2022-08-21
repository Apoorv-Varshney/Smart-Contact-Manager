console.log("js code integerated")

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")) {

		//cross button pressed
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {

		//show sidebar with content bar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
}

const search = () => {

	let query = $("#search-input").val();

	if (query == "") {
		$(".search-result").hide();
	} else {

		//sending request to server
		let url = `http://localhost:8080/search/${query}`;
		fetch(url)

			//getting response from url
			.then((response) => {
				return response.json();
			})

			//converting data into html
			.then((data) => {
				console.log(data);

				let text = `<div class='list-group'>`;
				data.forEach((contact) => {
					//getting contacts as link
					text += `<a href='/user/contact/${contact.cid}' 
					class='list-group-item list-group-action'> ${contact.name} </a>`;
				});

				text += `</div>`;

				//show filtered contacts on html page
				$(".search-result").html(text);
				$(".search-result").show();
			});


	}
}
