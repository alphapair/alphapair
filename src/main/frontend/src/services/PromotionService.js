import axios from "axios";
import ContentService from "./ContentService";

const PROMOTIONS_URL = "http://localhost:8080/api/v1/promotions";

class PromotionService {
    addPromotion = (promotion, callback) => {
        axios.post(PROMOTIONS_URL, promotion)
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.addPromotion, callback, promotion);
            }
        })
    }

    getPromotion = (promotionId, callback) => {
        axios.get(PROMOTIONS_URL + "/" + promotionId)
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.getPromotion, callback, promotionId);
            }
        });
    }

    getAllPromotions = (body, callback) => {
        axios.get(PROMOTIONS_URL)
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.getAllPromotions, callback, null);
            }
        });
    }

    updatePromotionField = (body, callback) => {
        let bodyWithoutId = JSON.parse(JSON.stringify(body));
        delete bodyWithoutId.id;
        let promotionId = body.id;
        axios.put(PROMOTIONS_URL + "/" + promotionId, bodyWithoutId)
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.updatePromotionField, callback, body);
            }
        });
    }

    deletePromotion = (promotionId, callback) => {
        axios.delete(PROMOTIONS_URL + "/" + promotionId)
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.deletePromotion, callback, promotionId);
            }
        });
    }

    getNewAssignment = (promotionId, callback) => {
        axios.put(PROMOTIONS_URL + "/" + promotionId +  "/assignment")
        .then((res) => callback(res.data))
        .catch((err) => {
            if (err.response) {
                ContentService.handleError(err.response.data, this.getNewAssignment, callback, promotionId);
            }
        });
    }
}

export default new PromotionService();