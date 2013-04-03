include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:org.testname"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
