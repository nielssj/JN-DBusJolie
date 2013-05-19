include "console.iol"
include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

execution { concurrent }

init
{
	print@Console("listening..\n")()
}

main
{
	twice ( number ) ( response ) {
		response = number * 2;
		print@Console("Received request: " + number + " = " + response + "\n")()
	}
}
