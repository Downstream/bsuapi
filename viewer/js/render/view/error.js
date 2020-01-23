import Element from "../element.js";

export default class ErrorView {
    build () {
        return [
            Element.title('Data not supported'),
            Element.subtitle('Sorry for the trouble.'),
            Element.text('The API response data does not fit a pattern this viewer was built to handle.'),
            Element.text('Maybe one day? Maybe copy-paste the url into an email to someone who can do something about it?'),
        ];
    }
}