/**
 * JavaScript zajistujici zobrazovani CoffeeSites v mape a obsluhujici udalosti kliknuti do mapy.
 * Pomoci konstrukce <![CDATA[ a [[${allSites}]] v komentari zajisti Thymeleaf vlozeni potrebnych dat z 
 * modelu do JavaScriptu
 */
		
		/*<![CDATA[*/    	
		var list = /*[[${allSites}]]*/ null;
		/*]]>*/
		
		var latSearch = document.getElementById("lat1");
		var lonSearch = document.getElementById("lon1");
		
		var lat1 = latSearch.value;
		var lon1 = lonSearch.value;
    	
		/* list je usporadan vzestupne, v poslednim zaznamu je bod s nejvetsi vzdalenosti od bodu hledani */
		/* Podle techto bodu se nastavi spravny zoom metodou fitBounds() */
		
		var bounds = null;
		
		if (list != null && list.length > 0) {
			var maxLat = list[list.length-1].zemSirka;
			var maxLon = list[list.length-1].zemDelka;
		
			var corner1 = L.latLng(lat1, lon1),
				corner2 = L.latLng(maxLat, maxLon),
				bounds = L.latLngBounds(corner1, corner2);
		}
		
		/* Deafault Latitude and longitude */
   		 if (lat1 == 0 && lon1 == 0) {
   			 lat1 = 49.8250401;
   			 lon1 = 15.4190817;
   		}
   		 
		var cfMap = L.map('map');
   		
   		L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
   		    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
   		    maxZoom: 18,
   		    id: 'mapbox.streets',
   		    accessToken: 'pk.eyJ1IjoibWljaGFsdjIiLCJhIjoiY2pvMWR2NHRhMDlxbjNwb2FjcDJnYnI5byJ9.HiXC2tFtvIIgk7hMqCQ-rA'
   		}).addTo(cfMap);
   		
		if (bounds != null) {
   			
			/*var intiMarker = L.marker([lat1, lon1]).addTo(cfMap);*/
			var circle = L.circle([lat1, lon1], {
			    color: 'red',
			    fillColor: '#f03',
			    fillOpacity: 0.5,
			    radius: 10
			}).addTo(cfMap);
			
			cfMap.fitBounds(bounds);
		}
   		else
   			cfMap.setView([lat1, lon1], 8);
   		
   		
   		cfMap.addSites = function addCoffeeSiteMarkers(coffeeSites) {
   			for (var i=0; i < coffeeSites.length; i++) {
   				var marker = L.marker([coffeeSites[i].zemSirka, coffeeSites[i].zemDelka]).addTo(cfMap);
   				var stars = "";
   		   		if (coffeeSites[i].averageStars > 0)
   		   			stars = "Stars: <b>" + coffeeSites[i].averageStars + "</b>"; 
   					marker.bindPopup("<html>" + coffeeSites[i].typPodniku.coffeeSiteType + "<br>"
   						                  + coffeeSites[i].typLokality.locationType + "<br>"
   						                  + coffeeSites[i].distFromSearchPoint + " m<br>"
   						                  + stars 
   						                  + "</html>").openPopup();
   			}
   		}
   		
   		/* Handle on-click to the map event */
   		/* Get coordinates and fill-in into search form */
		function onMapClick(e) {
			latSearch.value = e.latlng.lat;
			lonSearch.value = e.latlng.lng;
		}

		cfMap.on('click', onMapClick);
		
		if (list.length > 0)
			cfMap.addSites(list);
