include "simpleTypesInterface.iol"
include "console.iol"

inputPort SimpleTypesServer {
	Location: "dbus:/org.testname:/object"
	Interfaces: SimpleTypes
}

execution { concurrent }

main
{
	[ testM ( request ) ( response )  {
		println@Console(request)();
		response = 42
	} ] {nullProcess}
	[ getInt ( ) ( response )  {
		response = 42
	} ] {nullProcess}
	[ getBool ( ) ( response )  {
		response = true
	} ] {nullProcess}
	[ getString ( ) ( response ) {
		response = "John Doe"
	} ] {nullProcess}
}
