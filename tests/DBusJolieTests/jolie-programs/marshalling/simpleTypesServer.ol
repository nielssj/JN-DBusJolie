include "simpleTypesInterface.iol"

inputPort SimpleTypesServer {
	Location: "dbus:/org.testname:/object"
	Interfaces: SimpleTypes
}

execution { concurrent }

main
{
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