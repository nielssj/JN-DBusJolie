include "console.iol"
include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

init
{
	print@Console("listening..\n")()
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
