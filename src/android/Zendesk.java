package info.lonwi.cordova.plugin.zendesk;

import android.content.Context;
import android.util.Log;
import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import zendesk.core.JwtIdentity;
import zendesk.core.AnonymousIdentity;
import zendesk.core.Identity;
import zendesk.support.CustomField;
import zendesk.support.Support;
import zendesk.support.guide.HelpCenterActivity;
import zendesk.support.guide.ViewArticleActivity;
import zendesk.support.request.RequestConfiguration;
import zendesk.support.request.RequestActivity;
import zendesk.support.requestlist.RequestListActivity;

public class Zendesk extends CordovaPlugin {

    private static final String TAG = "Zendesk";
    private static final String ACTION_INITIALIZE = "initialize";
    private static final String ACTION_SET_IDENTITY = "setIdentity";
    private static final String ACTION_SET_ANONYMOUS_IDENTITY = "setAnonymousIdentity";
    private static final String ACTION_SHOW_HELP_CENTER = "showHelpCenter";
    private static final String ACTION_SHOW_HELP_CENTER_ARTICLE = "showHelpCenterArticle";
    private static final String ACTION_SHOW_TICKET_REQUEST = "showTicketRequest";
    private static final String ACTION_SHOW_USER_TICKETS = "showUserTickets";

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        switch(action) {
            case ACTION_INITIALIZE:
                String appId = args.getString(0);
                String clientId = args.getString(1);
                String zendeskUrl = args.getString(2);
                Log.d(TAG, "ACTION_INITIALIZE: appId: "+ appId + " clientId: " + clientId + " zendeskUrl " + zendeskUrl);
                zendesk.core.Zendesk.INSTANCE.init(this.getContext(), zendeskUrl, appId, clientId);
                Support.INSTANCE.init(zendesk.core.Zendesk.INSTANCE);
                break;
            case ACTION_SET_IDENTITY:
                String token = args.getString(0);
                Log.d(TAG, "ACTION_SET_IDENTITY:" + token);
                Identity identity = new JwtIdentity(token);
                zendesk.core.Zendesk.INSTANCE.setIdentity(identity);
                break;
            case ACTION_SET_ANONYMOUS_IDENTITY:
                String name = args.getString(0);
                String email = args.getString(1);

                Identity anonymousIdentity = new AnonymousIdentity.Builder()
                  .withNameIdentifier(name)
                  .withEmailIdentifier(email)
                  .build();

                zendesk.core.Zendesk.INSTANCE.setIdentity(anonymousIdentity);

                break;
            case ACTION_SHOW_HELP_CENTER:
                String groupType = args.getString(0);
                List<Long> groupIds;
                List<String> labels;


                if (!args.isNull(1)) {
                  groupIds = jsonArrayToList(args.getJSONArray(1));
                } else {
                  groupIds = new ArrayList<>();
                }

                if (!args.isNull(2)) {
                  labels = jsonArrayToList(args.getJSONArray(2));
                } else {
                  labels = new ArrayList<>();
                }

                zendesk.support.guide.HelpCenterConfiguration.Builder helpCenterActivityBuilder = HelpCenterActivity.builder();

                if ("category".equals(groupType) && !groupIds.isEmpty()) {
                  helpCenterActivityBuilder = helpCenterActivityBuilder.withArticlesForCategoryIds(groupIds);
                } else if ("section".equals(groupType) && !groupIds.isEmpty()) {
                  helpCenterActivityBuilder = helpCenterActivityBuilder.withArticlesForSectionIds(groupIds);
                }

                if (labels.size() > 0) {
                  helpCenterActivityBuilder = helpCenterActivityBuilder.withLabelNames(labels);
                }

                helpCenterActivityBuilder.show(this.cordova.getActivity());
                break;
            case ACTION_SHOW_HELP_CENTER_ARTICLE:
                String articleId = args.getString(0);
                ViewArticleActivity.builder(Long.parseLong(articleId)).show(this.cordova.getActivity());
                break;
            case ACTION_SHOW_TICKET_REQUEST:
                String subject = args.getString(0);
                List<String> tags;
                List<String> fields;

                if (!args.isNull(1)) {
                  tags = jsonArrayToList(args.getJSONArray(1));
                } else {
                  tags = new ArrayList<>();
                }

                if (!args.isNull(2)) {
                  fields = jsonArrayToList(args.getJSONArray(2));
                } else {
                  fields = new ArrayList<>();
                }

                RequestConfiguration.Builder requestActivityBuilder = RequestActivity.builder();

                if (subject != null) {
                  requestActivityBuilder = requestActivityBuilder.withRequestSubject(subject);
                }

                if (!tags.isEmpty()) {
                  requestActivityBuilder = requestActivityBuilder.withTags(tags);
                }

                if (!fields.isEmpty()) {
                  ArrayList<CustomField> mappedFields = new ArrayList();

                  for (String field: fields) {
                    String[] fieldParts = field.split("\\|");
                    if (fieldParts.length == 2) {
                      Long fieldId = Long.parseLong(fieldParts[0]);
                      String fieldValue = fieldParts[1];
                      CustomField customField = new CustomField(fieldId, fieldValue);
                      mappedFields.add(customField);
                    }
                  }
                  requestActivityBuilder = requestActivityBuilder.withCustomFields(mappedFields);
                }
                requestActivityBuilder.show(this.cordova.getActivity());
                break;
            case ACTION_SHOW_USER_TICKETS:
                RequestListActivity.builder().show(this.cordova.getActivity());
                break;
            default:
                callbackContext.error("Invalid action: " + action);
                return false;
        }
        Log.d(TAG, "callbackContext.success()");
        callbackContext.success();
        return true;
    }

    private Context getContext() {
      return this.cordova.getActivity().getApplicationContext();
    }

    private <T> List<T> jsonArrayToList(JSONArray jsonArray) {
      List<T> arrayList = new ArrayList<>();

      for (int i = 0; i < jsonArray.length(); i++) {
        try {
          arrayList.add((T)jsonArray.get(i));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      return arrayList;
    }

}
