# PowerShell script to generate protos for the Angular client on Windows
$ErrorActionPreference = "Stop"

$protoDir = "../server/proto"
$outputDir = "src/app/proto"

if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir
}

$protos = @(
    "$protoDir/client/model.proto",
    "$protoDir/client/driver_model.proto",
    "$protoDir/client/rotation_model.proto",
    "$protoDir/client/asset_model.proto",
    "$protoDir/client/asset_management.proto",
    "$protoDir/client/initialize_race.proto",
    "$protoDir/client/initialize_interface.proto",
    "$protoDir/client/update_interface_config.proto",
    "$protoDir/client/set_interface_pin_state.proto",
    "$protoDir/client/set_interface_rgb_led_state.proto",
    "$protoDir/client/interface_event.proto",
    "$protoDir/client/start_race.proto",
    "$protoDir/client/pause_race.proto",
    "$protoDir/client/next_heat.proto",
    "$protoDir/client/restart_heat.proto",
    "$protoDir/client/skip_heat.proto",
    "$protoDir/client/defer_heat.proto",
    "$protoDir/client/race_subscription.proto",
    "$protoDir/client/arduino_config.proto",
    "$protoDir/client/lane_model.proto",
    "$protoDir/client/track_model.proto",
    "$protoDir/client/race_model.proto",
    "$protoDir/client/team_model.proto",
    "$protoDir/client/audio_config.proto",
    "$protoDir/server/race_state.proto",
    "$protoDir/server/asset_management_response.proto",
    "$protoDir/server/race_time.proto",
    "$protoDir/server/lap.proto",
    "$protoDir/server/race_data.proto",
    "$protoDir/server/race.proto",
    "$protoDir/server/race_participant.proto",
    "$protoDir/server/heat_data.proto",
    "$protoDir/server/heat.proto",
    "$protoDir/server/standings_update.proto",
    "$protoDir/server/overall_standings_update.proto",
    "$protoDir/server/record_data.proto",
    "$protoDir/server/demo.proto",
    "$protoDir/server/full_update.proto"
)

Write-Host "Generating message.js..."
node node_modules/protobufjs-cli/bin/pbjs -p $protoDir -t static-module -w es6 -o $outputDir/message.js $protos

Write-Host "Generating message.d.ts..."
node node_modules/protobufjs-cli/bin/pbts -o $outputDir/message.d.ts $outputDir/message.js

Write-Host "Done."
