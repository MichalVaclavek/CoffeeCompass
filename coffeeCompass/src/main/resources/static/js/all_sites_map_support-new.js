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

var souradnice = []; /* souradnice vsech bodu/znacek v mape */
var allSites = [];
var markers = []; // all site's standard markers

var base_url = window.location.origin;

var previousMarker = null; // To keep track of the previously clicked marker


/**
 * Creates map ...
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
}

/**
 * Main function to be called from html to create map in given div element
 */
map.create = function() {
	 var	 latInit = 49.8250401;
	 var	 lonInit = 15.4190817;
	 createMap(latInit, lonInit);
}

/*
 * Inserts all CoffeeSites geo coordinates into the map as markers/icons with CoffeeSite image and some
 * additional info to be shown on the mark's card or in table
 */
map.insertSites = function(mAllSites) {

	if (mAllSites != null && mAllSites.length > 0) {
		allSites = mAllSites;
		// vytvoreni markeru pro kazdy site
		allSites.forEach(function(site) {
			// prirazeni id jednotlivemu markeru - vlastni id, jinak se generuje nahodne
            var znacka = L.marker(L.latLng(site.zemSirka, site.zemDelka), {icon: siteIcon, alt: site.id})
                          .bindPopup(createMarkerPopups(site))
                          .addTo(mapa);
            znacka.on('click', onMarkerClick);
			markers.push(znacka);
		});
	}
}

function onMarkerClick(e) {
    // vybrany marker
    var marker = e.target;
    var markerSiteExtId = marker.options.alt;

    // zobrazit data o lokaci v tab. - parovani vybraneho markeru pomoci jeho id a nasich vstupnich dat
    for (var i = 0; i < markers.length; i++) {
        if (allSites[i].id == markerSiteExtId) {
            if (previousMarker) {
              previousMarker.setIcon(siteIcon);
            }

            // Set the clicked marker's icon to the clickedIcon
            marker.setIcon(selectedSiteIcon);

            // Update the previousMarker to the currently clicked marker
            previousMarker = marker;

            fillInTable(allSites[i]);
            fillInListOfImages(allSites[i]);
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
    footerText += site.id + "'>Další detaily ...</a>";

    var popupText = headerText + textVizitka + footerText;
    return popupText;
}

fillInTable = function(site) {

    var siteNameTableCell = document.getElementById('siteName');
    var siteTypeTableCell = document.getElementById('coffeeSiteType');
    var locationTypeTableCell = document.getElementById('locationType');
    var priceRangeTableCell = document.getElementById('priceRange');
    var pristupnostDnyHodTableCell = document.getElementById('pristupnostDnyHod');
    var mestoTableCell = document.getElementById('mesto');
    var uliceTableCell = document.getElementById('uliceCP');
    var avgStarsTableCell = document.getElementById('avgStars');

    var siteImageDiv = document.getElementById('selectedImage');

    clearAll = function() {
        siteNameTableCell.innerHTML = '';
        siteTypeTableCell.innerHTML = '';
        locationTypeTableCell.innerHTML = '';
        priceRangeTableCell.innerHTML = '';
        pristupnostDnyHodTableCell.innerHTML = '';
        mestoTableCell.innerHTML = '';
        uliceTableCell.innerHTML = '';
        avgStarsTableCell.innerHTML = '';

        siteImageDiv.src = '';
    }

    clearAll();

    // fill table cells with site's data
    siteNameTableCell.innerHTML = site.siteName;
    siteTypeTableCell.innerHTML = site.typPodniku != null ? site.typPodniku.coffeeSiteType : '';
    locationTypeTableCell.innerHTML = site.typLokality != null ? site.typLokality.locationType : '';
    priceRangeTableCell.innerHTML = site.cena != null ? site.cena.priceRange : '';
    pristupnostDnyHodTableCell.innerHTML = site.pristupnostDny + ", " + site.pristupnostHod;
    mestoTableCell.innerHTML = site.mesto;
    uliceTableCell.innerHTML = site.uliceCP;
    avgStarsTableCell.innerHTML = site.averageStarsWithNumOfHodnoceni.common;

    siteImageDiv.src = site.mainImageURL;
}

fillInListOfImages = function(site) {
    var imagesList = document.getElementById('imagesList');
    // first clear the list of images
    imagesList.innerHTML = '';

    // call coffeesites image API (basic API) to get all imageFiles for given site and fill in the list of images using URLs of the images
    var xhr = new XMLHttpRequest();
    var imagesApiUrl = window.location.href;
//    imagesApiUrl = imagesApiUrl.replace("allSitesInMap", 'api/v1/images/object/' + site.id);
    imagesApiUrl = imagesApiUrl.replace("allSitesInMap", 'api/v1/coffeesites/image/allImageUrls/' + site.id);

    xhr.open('GET', imagesApiUrl, true);
    xhr.onload = function () {
        if (xhr.status >= 200 && xhr.status < 300) {
            var response = JSON.parse(xhr.responseText);
            if (response != null && response.length > 0) {
                var img;
                for (var i = 0; i < response.length; i++) {
                    img = document.createElement('img');
                    img.src = response[i];
                    img.style.height = "100px";
                    img.style.margin = "5px";
                    imagesList.appendChild(img);
                }
                addClickHandlerToImages();
            }
        } else {
            console.log('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}

// add click handler to every image inserted into the list of images
addClickHandlerToImages = function() {
    var images = document.querySelectorAll('#imagesList img');

    // if the clicked element is an image, show it in the selectedImage div
    var selectedImage = document.getElementById('selectedImage');

    images.forEach( function(image, index) {
        image.addEventListener('click', function() {
            // Copy the clicked image's source to the selectedImage element
            selectedImage.src = image.src;
            let indexToRemoveFrom = image.src.indexOf("&"); // to remove &variant=small in the path
            if (indexToRemoveFrom > 0) {
                selectedImage.src = image.src.substring(0, indexToRemoveFrom)
            };
        })
    });
}


var changeNodeVisibility = function(node) {
    node.disabled = !node.disabled;
    if (node.disabled) node.style.visibility = "hidden";
    if (!node.disabled) node.style.visibility = "visible";
}
