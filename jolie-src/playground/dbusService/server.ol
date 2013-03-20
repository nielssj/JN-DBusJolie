include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "socket://localhost:8000"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
