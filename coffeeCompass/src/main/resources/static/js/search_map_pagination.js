/** This JS file contains functions to show inserted list of CoffeeSites in the table body paginated **/

var current_page = 1;
var records_per_page = 10;

/* List of CoffeeSites to be shown in table */
var foundSitesInTable;
/* Reference to table body element, where the 'foundSitesInTable' are to be shown */
var tableBodyRef;


function prevPage()
{
    if (current_page > 1) {
        current_page--;
        changePage(current_page);
    }
}

function nextPage()
{
    if (current_page < numPages()) {
        current_page++;
        changePage(current_page);
    }
}
    
function changePage(page)
{
    var btn_next = document.getElementById("btn_next");
    var btn_prev = document.getElementById("btn_prev");
    var page_span = document.getElementById("pageInfo");
    
    clearTableBody();
 
    // Validate page
    if (page < 1) page = 1;
    if (page > numPages()) page = numPages();

    for (var i = (page-1) * records_per_page; i < (page * records_per_page) && i < foundSitesInTable.length; i++) {
    	insertTableRow(foundSitesInTable[i]);
    }
    
    page_span.innerHTML = page + "/" + numPages();

    if (page == 1) {
        btn_prev.style.visibility = "hidden";
    } else {
        btn_prev.style.visibility = "visible";
    }

    if (page == numPages()) {
        btn_next.style.visibility = "hidden";
    } else {
        btn_next.style.visibility = "visible";
    }
}

function numPages()
{
    return Math.ceil(foundSitesInTable.length / records_per_page);
}


clearTableBody = function() {
	$("#foundSitesTableBody").find("tr").remove();
}

/* Inserts new row of CoffeeSite data into table body */
insertTableRow = function(site) {
	
	// Insert a row in the table at the last row
	var newRow   = tableBodyRef.insertRow();
	newRow.setAttribute("style", "cursor: pointer;");
	newRow.onclick = function() {rowClicked(site.externalId)};
	
	insertTableRowCell(newRow, site.averageStarsWithNumOfHodnoceni.common);
	insertTableRowCell(newRow, site.distFromSearchPoint);
	insertTableRowCell(newRow, createStringFromList(site.otherOffers, 'offer'));
	insertTableRowCell(newRow, createStringFromList(site.coffeeSorts, 'coffeeSort'));
	insertTableRowCell(newRow, site.typPodniku.coffeeSiteType);
	insertTableRowCell(newRow, site.statusZarizeni.status);
	insertTableRowCell(newRow, site.mesto);
	insertTableRowCell(newRow, site.siteName);
}

/* Inserts one cell with given text into the row's 0 position i.e. at the beginning of row */
insertTableRowCell = function(row, text) {
	// Insert a cell in the row at index 0
	var newCell  = row.insertCell(0);

	// Append a text node to the cell
	var newText  = document.createTextNode(text);
	newCell.appendChild(newText);
}

/* Helper function to create list of items connected by ',' from the given array of objects and its property values */
createStringFromList = function(listOfValues, property) {
	var i;
	var text="";
	
	for (i = 0; i < listOfValues.length; i++) {
		 // if not last item, add next item and ',' at the end of result text
		 text += listOfValues[i][property] + ((i != listOfValues.length - 1) ? ", " : "");
	}
	return text;
}


/* Main function - inserts all items of foundSites into body table tableBodyReference  */
/* and shows page n. 1 of the foundSites in the table */
insertSitesToTable = function(foundSites, tableBodyReferenceLoc)
{
	if (foundSites != null && foundSites.length > 0)
	{	     		     	
		foundSitesInTable = foundSites;
		
		tableBodyRef = tableBodyReferenceLoc;
		
		changePage(1);
	}
}