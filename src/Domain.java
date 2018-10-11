import com.rabbitmq.client.BuiltinExchangeType;

public interface Domain {
    enum ExaminationType {
        KNEE("knee"),
        ELBOW("elbow"),
        HIP("hip");

        private final String type;

        ExaminationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

    }

    interface Exchange {
        String Name = "jacek";
        BuiltinExchangeType Type = BuiltinExchangeType.TOPIC;
    }

    interface TopicNames {
        String Log = "Log";
        String Info = "Info";
        String ExaminationRequest = "ExaminationRequest";
        String ExaminationResponse = "ExaminationResponse";

        static String createRequestTopic(ExaminationType type, String doctorName, String pacjent) {
            return ExaminationRequest + "." + type.getType() + "." + doctorName + "." + pacjent;
        }

        static String doctorAnyResponse(String doctorName) {
            return ExaminationResponse + ".*." + doctorName + ".*." +"*" ;
        }

        static String doctorExaminationResponse(String requestKey) {
            System.out.println("//////////////");
            int l = requestKey.lastIndexOf(".");
            String pacjent = requestKey.substring(l + 1);
            requestKey = requestKey.substring(0,l);
            int lastDotIndex = requestKey.lastIndexOf(".");
            String doctorName = requestKey.substring(lastDotIndex + 1);
            String examinationType = requestKey.substring(requestKey.indexOf(".") + 1, lastDotIndex);
            //System.out.println(ExaminationResponse + "." + examinationType + "." + doctorName + "." + pacjent);
            return ExaminationResponse + "." + examinationType + "."  + doctorName + "." + pacjent + ".done";

        }
    }
}

