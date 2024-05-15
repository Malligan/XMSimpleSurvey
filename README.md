# XMSimpleSurvey
XM Simple Survey android app

## Dagger
I skip setting up dagger for single screen which require injecting, i in my mind in this case it will be just for: viewmodel, repository, retrofit in single feature.

## Usecase
There is can be Usecase on top of viewmodel to prepare data for viewmodel, but i skipped that as it will be single function of converting one dto in another.

## Multiple uploading
In case when we allowed to store something longer than staying on screen it will be good idea to have component which keep submitting different questions to server. In current situation i choose to allow submitting single question at once.

## Questions loading error handling
Not covered by task, leave as is and show single question with message if backend return 0.

## Providing result
It is regular practice to provide result of loading in success state object, but i decide to keep it as separate property in state because list will be updated with answers and submissions.

## Testing
Added testing of 2 successful flows for this vm, as validating of answer not present in task i ignore possible testing of behavior related to consuming user input
