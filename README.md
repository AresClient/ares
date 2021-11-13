# <img src="https://aresclient.org/resources/icon.svg" alt="drawing" height="50"/> Ares
[![Build Status](https://travis-ci.com/AresClient/ares.svg?branch=master)](https://travis-ci.com/AresClient/ares)
[![Version](https://img.shields.io/badge/dynamic/json?color=success&label=1.12.2&prefix=v&query=%24%5B%271.12.2%27%5D%5B%27stable%27%5D%5B%27name%27%5D&url=https%3A%2F%2Faresclient.org%2Fapi%2Fv1%2Fdownloads.json)](https://aresclient.org/download)
[![Version](https://img.shields.io/badge/dynamic/json?color=success&label=1.16.4&prefix=v&query=%24%5B%271.16.4%27%5D%5B%27stable%27%5D%5B%27name%27%5D&url=https%3A%2F%2Faresclient.org%2Fapi%2Fv1%2Fdownloads.json)](https://aresclient.org/download)
[![Discord](https://img.shields.io/discord/650769808547119160?logo=discord)](https://discord.gg/GtBgknj)
[![License](https://img.shields.io/badge/license-LGPL%20v3-informational)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
![](https://img.shields.io/badge/skid%20free-100%25-informational)

Ares is a free, open source minecraft utility mod aimed at anarchy servers. Created by Tigermouthbear.

## Installing
Visit https://aresclient.org/download for instructions on downloading and installing.

## Building
- Requires JDK 17
- To build all versions of Ares (including the installer), run the `build` gradle task. 
  - To build the forge 1.12.2 version, run the `:ares-forge:build` gradle task.
  - To build the fabric 1.17.1 version, run the `:ares-fabric:build` gradle task.
  - To build the fabric 1.16.4 version, run the `:ares-fabric-1.16:build` gradle task.
  - To build the installer, run the `:ares-installer:build` gradle task. 
- All built jars will be copied to the `build` folder.

## Developer Environment
- To set up the development environment, run the `setupWorkspace` gradle task (this may take a while and produce warnings).
- For testing, run the `testForge` or `testFabric` gradle task to build and copy the mod into your mods folder automatically (currently only works for default minecraft folder).

## Contributing
Feel free to open a pull request or issue, we currently don't have templates for either of these so please be descriptive in your explanations. If you provide a pull request, please keep the same formatting as the rest of the project.