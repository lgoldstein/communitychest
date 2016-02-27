
/*
 * Creates a Google map attached to the specified document element and
 * centered at the specified address
 */
function makeMap(address,elementId,heightValue,widthValue) {
	var map = new google.maps.Map(document.getElementById(elementId),
				{
					height: heightValue,
					width: widthValue,
					zoom: 16,
					mapTypeId: google.maps.MapTypeId.ROADMAP
				});
	var geocoder = new google.maps.Geocoder();
	geocoder.geocode( { 'address': address }, function(results, status) {
		if (status == google.maps.GeocoderStatus.OK) {
			var locResult=results[0];
			var fmtAddress=locResult.formatted_address;
			console.log("Resolved location of " + address + ": " + fmtAddress);

			var geomValue=locResult.geometry;
			var geoLocation=geomValue.location;
			console.log("Location of " + address + ": " + geoLocation);
			map.setCenter(geoLocation);

			var marker = new google.maps.Marker(
					{
						map: map,
						position: geoLocation
					});
			marker.setTitle(fmtAddress);
			marker.setAnimation(google.maps.Animation.BOUNCE);
		} else {
			console.log("Cannot locate '" + address + "': " + status);
		} 
	});
}
