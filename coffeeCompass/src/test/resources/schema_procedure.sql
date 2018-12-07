--CREATE SCHEMA IF NOT EXISTS COFFEECOMPASS;

CREATE FUNCTION coffeecompass.distance(lat1 double, lon1 double, lat2 double, lon2 double) RETURNS DOUBLE 
BEGIN ATOMIC
DECLARE dist DOUBLE;
DECLARE latDist DOUBLE;
DECLARE lonDist DOUBLE;
DECLARE a DOUBLE;
DECLARE c DOUBLE;
DECLARE r DOUBLE;
DECLARE latDist DOUBLE;
DECLARE lonDist DOUBLE;
SET r = 6371000;
SET latDist = RADIANS( lat2 - lat1 );
SET lonDist = RADIANS( lon2 - lon1 );
SET a = POW( SIN( latDist/2 ), 2 ) + COS( RADIANS( lat1 ) ) * COS( RADIANS( lat2 ) ) * POW( SIN( lonDist / 2 ), 2 );
SET c = 2 * ATAN2( SQRT( a ), SQRT( 1 - a ) );
SET dist = r * c;
RETURN dist;
END