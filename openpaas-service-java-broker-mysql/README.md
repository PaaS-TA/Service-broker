#MySQL 자바 브로커

 MySQL 자바 Broker는 MySQL 데이터베이스를 OpenPaas 서비스로 제공합니다. <br>
 이 브로커는 클라우드 컨트롤러와 서비스 브로커 간의 v2 서비스 API를 보여줍니다.<br> 
 이 API는 클라우드 컨트롤러 API와 혼동되어서는 안됩니다.<br>

 브로커에는 MySQL 서버가 포함되어 있지 않습니다.<br>
 대신, MySQL 서버의 서비스를 관리하는 MySQL 자바 브로커를 배포하는 것을 의미합니다.<br>
 
 MySQL 자바 브로커는 MySQL 서버와 (OpenPaaS)클라우드 파운드리간의 서비스를 제공하는것을 말하며 독립 실행 하여 실행되는 MySql서버 응용 프로그램은 지원되지 않습니다.<br>
 브로커가 수행하는 MySQL 관리 작업은 다음과 같습니다.

 - 데이터베이스 인스턴스 프로비저닝 (생성)
 - 자격 증명 작성 (바인드)
 - 자격 증명 제거 (바인딩 해제)
 - 데이터베이스 인스턴스 프로비저닝 해제 (삭제)

[서비스팩 개발 가이드](https://github.com/OpenPaaSRnD/Documents-PaaSTA-1.0/blob/master/Development-Guide/ServicePack_develope_guide.md)의 API 개발 가이드를 참고하시면 아키텍쳐와 기술, 구현과 개발에 대해 자세히 알 수 있습니다.
