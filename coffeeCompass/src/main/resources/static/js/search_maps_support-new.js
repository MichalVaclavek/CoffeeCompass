const API_KEY = 'SNfZm2Dflr5gLlZ7fiV4YLDpmI40VYoDzsdJDB2YWqU';

var LeafIcon = L.Icon.extend({
    options: {
//        shadowUrl: '/images/cup_basic_16px.png',
        iconSize:     [16, 20],
        shadowSize:   [10, 10],
        iconAnchor:   [8, 10],
        shadowAnchor: [4, 62],
        popupAnchor:  [-6, -60]
    }
});

var siteIcon = new LeafIcon({iconUrl: '/images/cup_basic_16px.png'})
var selectedSiteIcon = new LeafIcon({iconUrl: '/images/cup_red2_16px.png'})


var mapa;

var latSearchInput; /* hidden input field for current searching point latitude */
var lonSearchInput; /* hidden input field for current searching point longitude */

var latSearchLabel; /* field to show current searching point latitude */
var lonSearchLabel; /* field to show current searching point longitude */

var rangeSearchLabel; /* field to show current searching range in m */ 

var souradnice = []; /* souradnice vsech bodu/znacek v mape */
var allSites = []; // all found sites
var markers = []; // all site's standard markers

var base_url = window.location.origin;

var previousMarker = null; // To keep track of the previously clicked marker

/**
 * Creates map with movable marker on the lat1, lon1 input coordinates
 * inserted in the list of all points (CoffeeSites) to be shown in map.
 */
createMap = function(lat1, lon1) {

     mapa = L.map('map').setView(L.latLng(lat1, lon1), 8);

     L.tileLayer(`https://api.mapy.cz/v1/maptiles/basic/256/{z}/{x}/{y}?apikey=${API_KEY}`, {
       minZoom: 0,
       maxZoom: 19,
       attribution: '<a href="https://api.mapy.cz/copyright" target="_blank">&copy; Seznam.cz a.s. a další</a>',
     }).addTo(mapa);

    const LogoControl = L.Control.extend({
      options: {
        position: 'bottomleft',
      },

      onAdd: function (mapa) {
        const container = L.DomUtil.create('div');
        const link = L.DomUtil.create('a', '', container);

        link.setAttribute('href', 'http://mapy.cz/');
        link.setAttribute('target', '_blank');
        link.innerHTML = '<img src="https://api.mapy.cz/img/api/logo.svg" />';
        L.DomEvent.disableClickPropagation(link);

        return container;
      },
    });

    // finally we add our LogoControl to the map
    new LogoControl().addTo(mapa);

    var marker = L.marker(L.latLng(lat1, lon1),

                           {title: 'Střed hledání, přesuň mě!', draggable: true })
                  .addTo(mapa);
	marker.on('dragend', onStopDrag);
}


function onStopDrag(e) {
    var marker = e.target;
    var latLng = marker.getLatLng();

    latSearchInput.value = latLng.lat;
    lonSearchInput.value = latLng.lng;

    latSearchLabel.innerHTML = latSearchInput.value.substring(0,10);
    lonSearchLabel.innerHTML = lonSearchInput.value.substring(0,10);
}

/**
 * Main function to be called from html to create map in given div element
 */
map.create = function(aLatSearchInput, aLonSearchInput, aLatSearchLabel, aLonSearchLabel) {

	 latSearchInput = aLatSearchInput;
	 lonSearchInput = aLonSearchInput;
	 latSearchLabel = aLatSearchLabel;
	 lonSearchLabel = aLonSearchLabel;
	 
	 var latInit = latSearchInput.value;
	 var lonInit = lonSearchInput.value;

	 if (latInit == 0 && lonInit == 0) {
		 latInit = 49.8250401;
		 lonInit = 15.4190817;
	 }
	 
	 latSearchLabel.innerHTML = latInit.substring(0, 10);
	 lonSearchLabel.innerHTML = lonInit.substring(0, 10);
	 
	 createMap(latInit, lonInit);
}

/*
 * Inserts all CoffeeSites geo coordinates into the map as markers/icons
 * with CoffeeSite image and some additional info to be shown on
 * the mark's popup.
 */
map.insertSites = function(mAllSites) {

	if (mAllSites != null && mAllSites.length > 0) {
		allSites = mAllSites;
		// vytvoreni markeru pro kazdy site
		allSites.forEach(function(site) {
            var znacka = L.marker(L.latLng(site.zemSirka, site.zemDelka), {icon: siteIcon, alt: site.externalId})
                          .bindPopup(createMarkerPopups(site))
                          .addTo(mapa);
            znacka.on('click', onMarkerClick);
			markers.push(znacka);
		});
	}

    var allSitesBounds = L.latLngBounds(extractPositionsToLatLng(mAllSites));
    mapa.fitBounds(allSitesBounds);
}

function onMarkerClick(e) {
    // vybrany marker
    var marker = e.target;
    var markerSiteExtId = marker.options.alt;

    // zobrazit data o lokaci v tab. - parovani vybraneho markeru pomoci jeho id a nasich vstupnich dat
    for (var i = 0; i < markers.length; i++) {
        if (allSites[i].externalId == markerSiteExtId) {
            if (previousMarker) {
              previousMarker.setIcon(siteIcon);
            }

            // Set the clicked marker's icon to the clickedIcon
            marker.setIcon(selectedSiteIcon);

            // Update the previousMarker to the currently clicked marker
            previousMarker = marker;
            break;
        }
    }
}

createMarkerPopups = function(site) {
    var headerText = "<b><span style='display:block;text-align:center; margin-bottom:10px;'>" + site.siteName + "</span></b><br>";

    var bodyImage = "";

    if (!(site.mainImageURL === "")) {
        bodyImage = "<img style='height:120px;' src='" + site.mainImageURL + "'/>";
        bodyImage += "<br><br>"; // to create space above text, because of rotated image
    }

    var textVizitka = bodyImage + "hodnoceni: " + site.averageStarsWithNumOfHodnoceni.common + "<br>";

    // Inserts link to CoffeeSite details page
    var footerText = "<a href='" + base_url + "/showSite/";
    footerText += site.externalId + "'>Další detaily ...</a>";

    var popupText = headerText + textVizitka + footerText;
    return popupText;
}


map.findCoordinatesAndInsertToForm = function(city) {
   geocode(city);
}

var changeNodeVisibility = function(node) {
    node.disabled = !node.disabled;
    if (node.disabled) node.style.visibility = "hidden";
    if (!node.disabled) node.style.visibility = "visible";
}

// function for calculating a bounding box from an array of coordinates
function bbox(coords) {
	let minLatitude = Infinity;
	let minLongitude = Infinity;
	let maxLatitude = -Infinity;
	let maxLongitude = -Infinity;

	coords.forEach(coor => {
		minLongitude = Math.min(coor[0], minLongitude);
		maxLongitude = Math.max(coor[0], maxLongitude);
		minLatitude = Math.min(coor[1], minLatitude);
		maxLatitude = Math.max(coor[1], maxLatitude);
	});

	return [
		[minLongitude, minLatitude],
		[maxLongitude, maxLatitude],
	];
}

async function geocode(query) {
    try {

        const url = new URL(`https://api.mapy.cz/v1/geocode`);

        url.searchParams.set('lang', 'cs');
        url.searchParams.set('apikey', API_KEY);
        url.searchParams.set('query', query);
        url.searchParams.set('limit', '15');
        [
          'regional.municipality',
          'regional.municipality_part',
          'regional.street',
          'regional.address'
        ].forEach(type => url.searchParams.append('type', type));

        const response = await fetch(url.toString(), {
          mode: 'cors',
        });
        const json = await response.json();
        processGeocodeResult(json.items);

      } catch (ex) {
            console.log(ex);
      }
}

function extractPositionsToLatLng(allSites) {
  if (!allSites || !Array.isArray(allSites)) {
    return [];
  }

  // Use the map function to transform each item to a LatLng object
  return allSites.map(site => {
    return L.latLng(site.zemSirka, site.zemDelka);
  });
}



/*
 * Callback function for SMap.Geocoder() call looking for city/place geo coordinates
 */
function processGeocodeResult(results) { /* Odpověď */
	
    var vysledky = results;
    var data = [];
    var selectCity = document.getElementById('selectCity');
    var cityNameInput = document.getElementById("cityName");
    var additionalCityLabel = document.getElementById("selectfromlistSpan");
	
    if (!results.length) {
        /* alert("Tohle neznáme."); */
		// nahradit select inputem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "hidden";
		
		latSearchInput.value = ""; // Coordinates not known, try to find against cityName only in CoffeeSites's 'poloha_mesto' column
		lonSearchInput.value = "";
		
		$(".se-pre-con").fadeIn("fast");
    	document.getElementById("searchByCoordinatesForm").submit();
        return;
    }
    
    if (vysledky.length === 1 || areAllFieldStringsSame(vysledky, 'name')) {
    	// nahradit select inputem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "hidden";
	
		var item = vysledky.shift()
		latSearchInput.value = item.position.lat; /* Zobrazi se tedy zatim jen posledni souradnice */
		lonSearchInput.value = item.position.lon;
		/* data.push(item.label + " (" + item.coords.toWGS84(2).reverse().join(", ") + ")"); */
		/* document.getElementById("cityName").value = item.label; */
		cityNameInput.value = item.name;
		
		$(".se-pre-con").fadeIn("fast");
		document.getElementById("searchByCoordinatesForm").submit();
    	
    } else  if (vysledky.length > 1) { // More then 1 result returned from mapy.cz, select options has to be shown to user
		// nahradit input selectem
		swapDivsWithClick(cityNameInput, selectCity);
		additionalCityLabel.style.visibility = "visible";
	
		var i = 0;
		// naplnit select vysledky
		while (vysledky.length) { /* Zobrazit všechny výsledky hledání */
    		var item = vysledky.shift();
	        
	        var opt = document.createElement('option');
	        opt.value = item.name;
	        opt.dataset[i+'0'] = item.position.lon; // i.e. opt.dataset[00] means id of the 1st item's longitude, opt.dataset[10] means id of the 2nd item's longitude
	        opt.dataset[i+'1'] = item.position.lat; // i.e. opt.dataset[01] means id of the 1st item's latitude, opt.dataset[11] means id of the 2nd item's latitude
	        opt.innerHTML = item.name  + ", " + item.location;;
	        selectCity.appendChild(opt);
	        i++;
	    }
    }
    return vysledky.length > 0;
}

function areAllFieldStringsSame(objects, field) {
    if (objects.length === 0) return true; // Consider an empty array as having all fields the same

    const firstString = objects[0][field];

    for (let i = 1; i < objects.length; i++) {
        if (objects[i][field] !== firstString) {
            return false;
        }
    }

    return true;
}
