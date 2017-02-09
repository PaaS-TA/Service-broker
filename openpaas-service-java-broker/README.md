# Open PaaS 서비스 자바 브로커
Open PaaS 서비스 자바 브로커는 OpenPaaS Cloud Controller와 서비스 브로커 사이의 규약 구현을 말한다.<br>
서비스 자바 브로커는 HTTP (or HTTPS) endpoints URI 형식으로 구현된다. <br>
하나 이상의 Service가 하나의 Broker 에 의해 제공 될 수 있고, <br>
로드 밸런싱이 가능하게 수평 확장성 있게 제공 될 수 있다.<br>

Open PaaS 서비스 자바 브로커 API 플랫폼은 Cloud Controller 와 Service Broker 사이의 규약에 대해 구현했다.

Open PaaS 서비스 자바 브로커 API는 OpenPaaS 마켓플레이스(독립된 서비스 등)와 OpenPaaS 서비스 브로커간의 Http 인터페이스를 정의합니다.<br>
서비스 브로커는 Open PaaS 서비스 자바 브로커 API가 서비스브로커 API를 구현하는 서비스 구성요소 입니다.

OpenPaaS플랫폼에서 OpenPaaS서비스 브로커 API를 통한 구현으로 마켓플레이스 플랫폼에대한 관리 규약을 변경, 규제 하여 통제할 수 있다.

서비스브로커 아키텍쳐에 관련된 사항은 
[서비스팩 개발 가이드](./Development-Guide/ServicePack_develope_guide.md)의 API 개발 가이드를 참고하시면 아키텍쳐와 기술, 구현과 개발에 대해 자세히 알 수 있습니다.
 