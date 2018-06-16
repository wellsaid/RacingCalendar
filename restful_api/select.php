<?php

// ================= DEFINITIONS =================
// Tables accessible in the public API
$available_tables = array("series","events");

// Relative path of the database login detail file (it should be in a secure location)
$db_file = "./db/db.php";
// ============================================

// get the table requested
$request = explode('/', trim($_SERVER['PATH_INFO'],'/'));
if (sizeof($request) > 1){
	http_response_code(400);
	die("ERROR 400: Bad request!");
}
$table = $request[0];

// check if the table is one of the available
if(!in_array($table, $available_tables)){
	http_response_code(404);
	die("ERROR 404: Table $table not found!");
}

// build select query
$query = "SELECT * FROM `$table`";

if(sizeof($_GET) > 0){
	$query = $query." WHERE ";

	$first = 1;
	foreach (array_keys($_GET) as &$key){
		if($first == 1){
			$first = 0;
		} else {
			$query = $query."AND ";
		}

		$value = $_GET[$key];
		$query = $query."$key = '$value' ";
	}
}

// connect to the mysql database
require_once($db_file);
$link = mysqli_connect('localhost', DB_USER, DB_PASSWD, DB_NAME);
mysqli_set_charset($link,'utf8');

// excecute SQL statement
$result = mysqli_query($link,$query);

// die if SQL statement failed
if (!$result) {
  http_response_code(400);
  die(mysqli_error($link));
}

// print results, insert id or affected row count
echo '<pre>' . PHP_EOL;
echo '[' . PHP_EOL;
for ($i=0;$i<mysqli_num_rows($result);$i++) {
	echo ($i>0?',':'').json_encode(mysqli_fetch_object($result), JSON_PRETTY_PRINT);
}
echo ']' . PHP_EOL;
echo PHP_EOL . "</pre>"; 

// close mysql connection
mysqli_close($link);

?>
