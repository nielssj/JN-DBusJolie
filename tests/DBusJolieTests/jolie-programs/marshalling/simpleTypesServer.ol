include "simpleTypesInterface.iol"

inputPort SimpleTypesServer {
	Location: "dbus:/org.testname:/"
	Interfaces: SimpleTypes
}


execution { concurrent }

main
{
	getInt ( ) ( response ) {
		response = 42
	},
	getBool ( ) ( response ) {
		response = true
	},
	getString ( ) ( response ) {
		response = "John Doe"
	}
}