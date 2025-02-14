const API_KEY = 'SNfZm2Dflr5gLlZ7fiV4YLDpmI40VYoDzsdJDB2YWqU';

var LeafIcon = L.Icon.extend({
    options: {
        iconSize:     [16, 20],
        shadowSize:   [10, 10],
        iconAnchor:   [8, 10],
        shadowAnchor: [4, 62],
        popupAnchor:  [-6, -60]
    }
});

var mapa;

var latSearchInput; /* hidden input field for current searching point latitude */
var lonSearchInput; /* hidden input field for current searching point longitude */

var coffeeSiteLatInput; /* input field for CoffeeSite's latitude */
var coffeeSiteLonInput; /* input field for CoffeeSite's longitude */

var base_url = window.location.origin;

var marker; // přesouvatelná znacka pro označení polohy kávové lokace

map.create = function(aLatSearchInput, aLonSearchInput, aCityName, aIsMapVisible) {

	coffeeSiteLatInput = aLatSearchInput;
	coffeeSiteLonInput = aLonSearchInput;

	var latInit = coffeeSiteLatInput.value;
	var lonInit = coffeeSiteLonInput.value;

	isMapVisible = aIsMapVisible;

	// Show marker in the city only if aLatSearchInput and aLonSearchInput are empty
	if (aCityName.length > 1 && aIsMapVisible && latInit == 0 && lonInit == 0) {
		map.findCoordinatesAndShowInMap(aCityName);
	} else {
		createMap(latInit, lonInit);
	}
}

/**
 * Creates map with movable marker on the lat1, lon1 input coordinates
 */
createMap = function(lat1, lon1) {

    if (lat1 == 0 && lon1 == 0) {
        lat1 = 49.8250401;
        lon1 = 15.4190817;
    }

    if (mapa == null) {

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

        marker = L.marker(L.latLng('49.8250401', '15.4190817'), {title: 'Vyber polohu, přesuň mě!', draggable: true })
                  .addTo(mapa);
        marker.on('dragend', onStopDrag);

         function onStopDrag(e) {
            var marker = e.target;
            var latLng = marker.getLatLng();

            coffeeSiteLatInput.value = latLng.lat;
            coffeeSiteLonInput.value = latLng.lng;
         }
    }
}

/* Finding coordinates of the City/town from api.mapy.cz */
map.findCoordinatesAndShowInMap = function(city) {
    geocode(city);
}

/* To indicate if the map is to be refreshed when new city name is selected in suggest.addListener() */
map.setMapVisible = function(aIsMapVisible) {
	isMapVisible = aIsMapVisible;
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

/*
 * Callback function for URL(`https://api.mapy.cz/v1/geocode`) call looking for city/place geo coordinates
 */
function processGeocodeResult(results) { /* Odpověď */

    var vysledky = results;
    var data = [];
    var cityNameInput = document.getElementById('cityInput').value;

    if (!results.length) { // žádný výsledek
        coffeeSiteLatInput.value = ""; // Coordinates not known
        coffeeSiteLonInput.value = "";
        return false;
    }

    if (vysledky.length === 1 || areAllFieldStringsSame(vysledky, 'name')) {
        var item = vysledky.shift()
        zoomToCity(item);
    } else if (vysledky.length > 1) { // More then 1 result returned from mapy.cz, select the result equal to input city name
        var i = 0;
        while (vysledky.length) { /* Zobrazit první souhlasný výsledek hledání */
            var item = vysledky.shift();
            if (cityNameInput == item.name) {
                break;
            }
            i++;
        }
        if (i < vysledky.length) {
            zoomToCity(item);
        } else {
            createMap(0, 0)
        }
    }

    return vysledky.length > 0;
}

/* Přenos informací o nalezených souřadnicích města do mapy a vstupního fieldu pro souřadnice */
function zoomToCity(geoCodeReturnItem) {
    var cityLat = geoCodeReturnItem.position.lat;
    var cityLon = geoCodeReturnItem.position.lon;

    coffeeSiteLatInput.value = cityLat;
    coffeeSiteLonInput.value = cityLon;
    mapa.setView(L.latLng(cityLat, cityLon), 12);
    marker.setLatLng(L.latLng(cityLat, cityLon));
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
