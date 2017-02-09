#Public API 자바 브로커

공공 데이터 포털(data.go.kr)에 소개된 공공 API를 OpenPaaS의 서비스로 제공합니다.<br>
이 브로커는 클라우드 컨트롤러와 서비스 브로커 간의 v2 서비스 API를 보여줍니다.<br> 
이 API는 클라우드 컨트롤러 API와 혼동되어서는 안됩니다.<br>

사용자는 공공 데이터 포털(data.go.kr)을 통해 API 사용 신청을 하고 인증키를 발급받습니다. 서비스 브로커 소스의 프로퍼티 파일('src/main/resources/application-mvc.properties')에 발급받은 인증키와 엔드포인트 등 API 정보를 입력하고 빌드합니다. 빌드된 서비스 브로커 war파일을 구동하고 이 서비스 브로커를 OpenPaaS에 등록하면 공공 OpenAPI를 OpenPaaS의 다른 서비스와 동일한 형태로 사용이 가능합니다.<br>

API 서비스 등록에 대한 상세한 설명은 [Public API 개발 가이드](https://github.com/OpenPaaSRnD/Documents-PaaSTA-1.0/blob/master/Development-Guide/PublicAPI_devlope_guide.md) 문서의 [6.1. 공공 API 서비스 브로커 설정파일 정의](https://github.com/OpenPaaSRnD/Documents-PaaSTA-1.0/blob/master/Development-Guide/PublicAPI_devlope_guide.md#44)을 참고합니다.

브로커가 수행하는 API 서비스 관리 작업은 다음과 같습니다.

- OpenAPI 서비스 인스턴스 프로비저닝 (생성)
- 자격 증명 작성 (바인드)
- 자격 증명 제거 (바인딩 해제)
- OpenAPI 서비스 인스턴스 프로비저닝 해제 (삭제)

[서비스팩 개발 가이드](https://github.com/OpenPaaSRnD/Documents-PaaSTA-1.0/blob/master/Development-Guide/ServicePack_develope_guide.md)의 API 개발 가이드를 참고하시면 아키텍쳐와 기술, 구현과 개발에 대해 자세히 알 수 있습니다.
