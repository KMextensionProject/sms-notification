package io.github.kmextensionproject.notification.sms;

import static com.twilio.rest.api.v2010.account.Message.creator;
import static io.github.kmextensionproject.notification.base.NotificationResult.failure;
import static io.github.kmextensionproject.notification.base.NotificationResult.success;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;

import io.github.kmextensionproject.notification.base.Message;
import io.github.kmextensionproject.notification.base.Notification;
import io.github.kmextensionproject.notification.base.NotificationResult;
import io.github.kmextensionproject.notification.base.Recipient;

/**
 * This class uses Twilio API to send simple SMS messages.<br>
 * To use this class properly one must provide following environment variables:
 * <ul>
 *  <li>${sms_twilio_sid} - your Twilio account SID</li>
 *  <li>${sms_twilio_token} - your Twilio auth token</li>
 *  <li>${sms_twilio_phone} - the phone number provided by twilio</li>
 * </ul>
 *
 * @author mkrajcovic
 */
public class SmsNotification implements Notification {

	private static final Logger LOG = Logger.getLogger(SmsNotification.class.getName());

	private String accountSid;
	private String authToken;
	private PhoneNumber senderPhone;

	// based on proper configuration
	private boolean sendingEnabled = true;

	public SmsNotification() {
		accountSid = getSystemProperty("sms_twilio_sid");
		authToken = getSystemProperty("sms_twilio_token");
		senderPhone = new PhoneNumber(getSystemProperty("sms_twilio_phone"));
		if (sendingEnabled) {
			Twilio.init(accountSid, authToken);
		}
	}

	private String getSystemProperty(String key) {
		String value = System.getenv(key);
		if (isNull(value)) {
			LOG.warning(() -> "${" + key + "} environment varible must be set to use SMS notifications");
			disableSending();
		}
		return value;
	}

	@Override
	public NotificationResult sendNotification(Message message, Recipient recipient) {
		requireNonNull(message, "message cannot be null");
		requireNonNull(recipient, "recipient cannot be null");

		if (!sendingEnabled) {
			return failure("Can not send an SMS - missing proper configuration, check logs for missing variables"); 
		} else if (isBlank(message.getBody()) || isBlank(recipient.getPhoneNumber())) {
			return failure("Can not send an SMS - message body or recipient's phone number is missing");
		}
		try {
			PhoneNumber to = new PhoneNumber(recipient.getPhoneNumber());
			creator(to, senderPhone, message.getBody()).create();
			return success("SMS sent successfully to " + recipient.getEmail());
		} catch (Exception ex) {
			return failure("Could not send an SMS to " + recipient.getPhoneNumber(), ex);
		}
	}

	private boolean isBlank(String value) {
		return isNull(value) || value.trim().isEmpty();
	}

	private void disableSending() {
		sendingEnabled = false;
	}

}
