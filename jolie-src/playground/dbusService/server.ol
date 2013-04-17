include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:session:/me/soeholm/coolbus"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
