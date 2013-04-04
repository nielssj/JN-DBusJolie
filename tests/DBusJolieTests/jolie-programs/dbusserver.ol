include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:org.testname:/object"
	//Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
