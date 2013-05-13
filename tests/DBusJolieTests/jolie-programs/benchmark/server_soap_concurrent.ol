include "console.iol"
include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "socket://localhost:8888"
	Protocol: soap
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
		response = number * 2
	}
}
