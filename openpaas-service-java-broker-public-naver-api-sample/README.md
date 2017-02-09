# Public & Naver Open API 샘플 앱
Public & Naver Open API 샘플 앱은 OpenPaaS에서 구동되는 애플리케이션이 서비스 브로커를 통해서 등록된 서비스를 사용하는 방법을 설명하기 위해서 구현된 샘플 애플리케이션입니다. 애플리케이션은 현재 진행되고 있는 전시/공연/축제의 위치를 지도에 표시해주는 기능을 가지고 있습니다. 이를 위해, 사용된 Open API는 다음과 같습니다.

공공 API
- 공연전시 정보 조회 서비스 (http://www.culture.go.kr)
- 인천광역시 문화행사 (http://iq.ifac.or.kr/)
- 대전광역시 문화축제 (http://data.daejeon.go.kr/)
- 전시공연/테마파크 정보 (http://api.namdokorea.com/)

네이버 API
- 네이버 지도 서비스 (http://www.naver.com)
- 네이버 주소-좌표 변환 서비스 (http://www.naver.com)
- 네이버 검색 서비스 (http://www.naver.com)

애플리케이션 구동을 위해서는 상기 7개의 Open API에 대한 사용 신청 및 인증키 획득이 선행되어야 합니다. 또한 Public API 서비스 브로커, Naver API 서비스 브로커의 프로퍼티 파일('src/main/resources/application-mvc.properties')에 각 API에 대한 사용 정보가 입력된 상태로 각 서비스 브로커가 빌드되어 동작하고 있어야 합니다.

상세한 내용은 [Public API 개발 가이드](https://github.com/OpenPaaSRnD/Documents-PaaSTA-1.0/blob/master/Development-Guide/PublicAPI_devlope_guide.md) 문서를 참고해 주시기 바랍니다.
