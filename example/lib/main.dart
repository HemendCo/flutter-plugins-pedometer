import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:pedometer/pedometer.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Pedometer ped = Pedometer();
  bool _isStarted = false;
  StepData _data;
  List<StepData> all;

  @override
  void initState() {
    super.initState();
    update();
    ped.events(onSensorChanged: (StepData sd) async {
      all = await ped.getAll();

      if (!mounted) return;

      setState(() {
        _data = sd;
      });
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  update() async {
    bool isStarted;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      isStarted = await ped.isStarted();
      _data = await ped.getToday();
      all = await ped.getAll();
    } on PlatformException {
      isStarted = false;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to up_date our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _isStarted = isStarted;
    });
  }

  Widget getDataWidgets() {
    List<Widget> list = new List<Widget>();

    for(var i = 0; all != null && i < all.length; i++) {
      StepData sd = all[i];

      list.add(RichText(
        text: TextSpan(
            children: <TextSpan> [
              TextSpan(
                  text: sd.date + ": ",
                  style: TextStyle(color: Colors.black38, fontSize: 15, fontWeight: FontWeight.normal)
              ),
              TextSpan(
                  text: sd.step.toString(),
                  style: TextStyle(color: Colors.redAccent, fontSize: 15, fontWeight: FontWeight.bold)
              )
            ]
        ),
      ));
    }

    if(list.length < 1) {
      list.add(
          Center(
            child: Text("Is empty", style: TextStyle(color: Colors.black26),),
          )
      );
    }

    return Expanded(
        child: ListView(
          shrinkWrap: true,
          children: list,
        )
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body:
          Padding(
            padding: EdgeInsets.fromLTRB(30.0, 50.0, 30.0, 50.0),
            child: Center(
                child: Column(
                  children: <Widget> [
                    Text('Running on: '+(_isStarted ? "Yes" : "No")+'\n', style: TextStyle(color: _isStarted ? Colors.green : Colors.red, fontWeight: FontWeight.bold)),
                    Row(
                      children: <Widget>[
                        MaterialButton(
                            color: _isStarted ? Colors.green : Colors.white,
                            highlightColor: Colors.green,
                            padding: EdgeInsets.fromLTRB(10.0, 5.0, 10.0, 5.0),
                            onPressed: () {
                              ped.start();
                              update();
                            },
                            child: Text('Start',textAlign: TextAlign.center, style: TextStyle(color: _isStarted ? Colors.white : Colors.black))
                        ),
                        Spacer(),
                        MaterialButton(
                            color: _isStarted ? Colors.white : Colors.red,
                            highlightColor: Colors.red,
                            padding: EdgeInsets.fromLTRB(10.0, 5.0, 10.0, 5.0),
                            onPressed: () {
                              ped.stop();
                              update();
                            },
                            child: Text('Stop', textAlign: TextAlign.center, style: TextStyle(color: _isStarted ? Colors.black : Colors.white))
                        )
                      ],
                    ),
                    Divider(height: 20, color: Colors.transparent,),
                    Text(_data != null && _data.date != null ? _data.date : "-", style: TextStyle(color: Colors.black38, fontSize: 15, fontWeight: FontWeight.bold)),
                    Divider(height: 10, color: Colors.transparent,),
                    Text(_data != null && _data.step != null ? _data.step.toString() : "-", style: TextStyle(color: Colors.redAccent, fontSize: 25, fontWeight: FontWeight.bold)),
                    MaterialButton(
                        color: Colors.orangeAccent,
                        highlightColor: Colors.orangeAccent,
                        padding: EdgeInsets.fromLTRB(10.0, 5.0, 10.0, 5.0),
                        onPressed: () {
                          ped.reset();
                          update();
                        },
                        child: Text('Reset',textAlign: TextAlign.center, style: TextStyle(color: Colors.white))
                    ),
                    Divider(height: 10),
                    getDataWidgets()
                  ],
                )

            ),
          )
      ),
    );
  }
}
