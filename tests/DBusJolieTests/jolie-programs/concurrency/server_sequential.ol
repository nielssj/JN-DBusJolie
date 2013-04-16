include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

execution { sequential }

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
