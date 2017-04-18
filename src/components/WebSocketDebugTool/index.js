import React from 'react';
import {Form, Icon, Input, Button, Checkbox} from 'antd';
import SockJS from 'sockjs-client';
import StompJS from 'stompjs/lib/stomp.js';
import './index.less';

const FormItem = Form.Item;
const CheckboxGroup = Checkbox.Group;
const Stomp = StompJS.Stomp;

/**
 * 用于调试websocket的一个小工具, 支持sockjs和stomp
 */
class WebSocketDebugTool extends React.PureComponent {

  state = {
    // 全局状态
    connected: false,  // 是否已经建立连接
    sockjs: false,  // 是否启用sockjs
    stomp: false,  // 是否启用stomp

    // controlled components相关状态
    url: '',
    stompConnectHeader: '',
    stompSubscribeDestination: '',
    stompSendHeader: '',
    stompSendDestination: '',
    messageContent: '',

    // 在console中显示的信息
    message: [],
  };

  /**
   * 连接服务端
   */
  connect = () => {
    const that = this;
    try {
      let client;
      // 对于STOMP和非STOMP要分别处理
      // sockjs和raw websocket的API是一样的
      if (this.state.stomp) {
        if (this.state.sockjs) {
          client = Stomp.over(new SockJS(this.state.url));
        } else {
          client = Stomp.over(new WebSocket(this.state.url));
        }

        let connectHeader = {};
        // header必须是个正确的json
        if (this.state.stompConnectHeader.length !== 0) {
          try {
            connectHeader = JSON.parse(this.state.stompConnectHeader)
          } catch (e) {
            console.error('parse STOMP connect header error %o', e);
            this.error(`STOMP connect header format error: ${this.state.stompConnectHeader}`);
            return;
          }
        }

        client.connect(connectHeader, () => {
          that.setState({connected: true});
          that.info(`Connect STOMP server success, url = ${that.state.url}, connectHeader = ${that.state.stompConnectHeader}`)
        });

      } else {
        if (this.state.sockjs) {
          client = new SockJS(this.state.url);
        } else {
          client = new WebSocket(this.state.url);
        }

        client.binaryType = 'arraybuffer';
        client.onopen = e => {
          console.debug('Connect success %o', e);
          that.info(`Connect success, url = ${that.state.url}`);
          that.setState({connected: true});
        };
        client.onmessage = e => {
          console.debug('Received message %o', e);
          that.info(`Received message: ${e.data}`);
        };
        client.onerror = e => {
          console.error('Connect error %o', e);
          that.error(`Connect error, message = ${e.data}, view chrome console for detail`);
          that.setState({connected: false});
        };
      }

      this.client = client;

    } catch (e) {
      console.error('Connect error %o', e);
      that.error(`Connect error, message = ${e.message}, view chrome console for detail`);
      that.setState({connected: false});
      return;
    }
  };

  /**
   * 关闭连接
   */
  disconnect = () => {
    if (!this.state.connected) {
      this.error(`Not Connected Yet`);
      return;
    }

    try {
      if (this.state.stomp) {
        this.client.disconnect();
      } else {
        this.client.close();
      }
      this.info('Close Connection Success');
      this.setState({connected: false});
    } catch (e) {
      console.log('disconnect fail %o', e);
      this.error(`disconnect fail, message = ${e.message}, view chrome console for detail`);
    }
  };

  /**
   * 发送消息
   */
  send = () => {
    try {
      if (this.state.stomp) {
        let headerJSON = {};
        // 如果是stomp协议, 必须校验一些条件

        // 必须有destination
        if (this.state.stompSendDestination.length === 0) {
          this.error(`STOMP send destination can not be empty.`);
          return;
        }
        // header必须是个正确的json
        if (this.state.stompSendHeader.length !== 0) {
          try {
            headerJSON = JSON.parse(this.state.stompSendHeader)
          } catch (e) {
            console.error('parse STOMP send header error %o', e);
            this.error(`STOMP send header format error: ${this.state.stompSendHeader}`);
            return;
          }
        }

        this.client.send(this.state.stompSendDestination, headerJSON, this.state.messageContent);
        this.info(`send STOMP message, destination = ${this.state.stompSendDestination}, content = ${this.state.messageContent}, header = ${this.state.stompSendHeader}`);
      } else {
        this.client.send(this.state.messageContent);
        this.info(`send message, content = ${this.state.messageContent}`);
      }
    } catch (e) {
      console.log('send message fail %o', e);
      this.error(`send message fail, message = ${e.message}, view chrome console for detail`);
    }
  };

  /**
   * stomp subscribe
   */
  subscribe = () => {
    if (this.state.stompSubscribeDestination.length === 0) {
      this.error(`STOMP subscribe destination can not be empty.`);
      return;
    }

    if (!this.state.stomp) {
      this.error(`Not in STOMP mode`);
      return;
    }

    if (!this.state.connected) {
      this.error(`Not Connected yet`);
      return;
    }

    try {
      this.client.subscribe(this.state.stompSubscribeDestination, this.getSubscribeCallback(this.state.stompSubscribeDestination));
      this.info(`subscribe destination ${this.state.stompSubscribeDestination} success`);
    } catch (e) {
      console.error('subscribe fail: %o', e);
      this.error(`subscribe destination ${this.state.stompSubscribeDestination} fail, message = ${e.message}`);
    }
  };

  getSubscribeCallback(destination) {
    return content => {
      this.info(`Receive subscribed message from destination ${destination}, content = ${content}`)
    };
  }


  // controlled component相关handle方法
  // 挺蛋疼的...


  handleUrlChange = e => {
    this.setState({url: e.target.value});
  };

  handleConnectTypeChange = value => {
    let sockjs = false;
    let stomp = false;
    for (const tmp of value) {
      if (tmp === 'SockJS') {
        sockjs = true;
      } else if (tmp === 'STOMP') {
        stomp = true;
      }
    }
    this.setState({sockjs, stomp});
  };

  handleStompConnectHeaderChange = e => {
    this.setState({stompConnectHeader: e.target.value});
  };

  handleStompSubscribeDestinationChange = e => {
    this.setState({stompSubscribeDestination: e.target.value});
  };

  handleStompSendHeaderChange = e => {
    this.setState({stompSendHeader: e.target.value});
  };

  handleStompSendDestinationChange = e => {
    this.setState({stompSendDestination: e.target.value});
  };

  handleMessageContentChange = e => {
    this.setState({messageContent: e.target.value});
  };

  /**
   * 输出info级别信息
   *
   * @param message
   */
  info = (message) => {
    this.log(`_INFO_:${message}`);
  };

  /**
   * 输出error级别信息
   *
   * @param message
   */
  error = (message) => {
    this.log(`_ERROR_:${message}`);
  };

  /**
   * 在console中新增一行输出
   *
   * @param message
   */
  log = (message) => {
    const length = this.state.message.length;
    const newMessage = this.state.message.slice(0, length);
    newMessage.push(message);
    this.setState({message: newMessage}, this.scrollToBottom);
  };

  /**
   * 将console div滚动到底部
   */
  scrollToBottom() {
    const scrollHeight = this.consoleOutput.scrollHeight;
    const height = this.consoleOutput.clientHeight;
    const maxScrollTop = scrollHeight - height;
    this.consoleOutput.scrollTop = maxScrollTop > 0 ? maxScrollTop : 0;
  }

  /**
   * 清除console div中的内容
   */
  clearOutput = () => {
    this.setState({message: []});
  };


  render() {

    const connectTypeArray = [];
    if (this.state.sockjs) {
      connectTypeArray.push('SockJS');
    }
    if (this.state.stomp) {
      connectTypeArray.push('STOMP');
    }

    // 我的布局真是一团糟...
    return <div style={{ height:'100%' }}>

      {/*fork me on github*/}
      <a href="https://github.com/jiangxy">
        <img style={{position: 'absolute', top: 0, right: 0, border: 0}}
             src="https://camo.githubusercontent.com/652c5b9acfaddf3a9c326fa6bde407b87f7be0f4/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6f72616e67655f6666373630302e706e67"
             alt="Fork me on GitHub"
             data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_right_orange_ff7600.png"/>
      </a>

      {/*common input*/}
      <div className="common">
        <Form inline>
          <FormItem label="URL">
            <Input prefix={<Icon type="link" style={{ fontSize: 13 }} />}
                   placeholder="URL to connect, 'ws://' for raw WebSocket or 'http://' for SockJS"
                   style={{ width:'400px' }} value={this.state.url} onChange={this.handleUrlChange}
                   disabled={this.state.connected}/>
          </FormItem>
          <FormItem>
            <Button type="primary"
                    onClick={this.state.connected ? this.disconnect : this.connect}>{this.state.connected ? 'Disconnect' : 'Connect'}</Button>
          </FormItem>
        </Form>
        <Form inline>
          <FormItem label="Connect Type">
            <CheckboxGroup options={[{label:'SockJS',value:'SockJS'},{label:'STOMP',value:'STOMP'}]}
                           value={connectTypeArray} onChange={this.handleConnectTypeChange}
                           disabled={this.state.connected}/>
          </FormItem>
        </Form>
      </div>

      {/*STOMP相关输入项, 注意disabled条件, 必须上面勾选STOMP后才可用*/}
      <div className="stomp">
        <pre>available if Connect Type = STOMP</pre>
        <Form inline style={{ marginTop:'5px' }}>
          <FormItem label="STOMP connect header">
            <Input placeholder='json string, e.g. {"header1":"value1", "header2":"value2"}' style={{ width:'400px' }}
                   value={this.state.stompConnectHeader} onChange={this.handleStompConnectHeaderChange}
                   disabled={!(this.state.stomp && !this.state.connected)}/>
          </FormItem>
        </Form>
        <Form inline style={{ marginTop:'5px' }}>
          <FormItem label="STOMP subscribe destination">
            <Input placeholder="e.g. /topic/test" style={{ width:'230px' }} value={this.state.stompSubscribeDestination}
                   onChange={this.handleStompSubscribeDestinationChange}
                   disabled={!(this.state.stomp && this.state.connected)}/>
          </FormItem>
          <FormItem>
            <Button type="primary" disabled={!(this.state.stomp && this.state.connected)} onClick={this.subscribe}>Subscribe</Button>
          </FormItem>
        </Form>
        <Form inline style={{ marginTop:'5px' }}>
          <FormItem label="STOMP send header">
            <Input placeholder='json string, e.g. {"header1":"value1", "header2":"value2"}' style={{ width:'400px' }}
                   value={this.state.stompSendHeader} onChange={this.handleStompSendHeaderChange}
                   disabled={!(this.state.stomp && this.state.connected)}/>
          </FormItem>
        </Form>
        <Form inline style={{ marginTop:'5px' }}>
          <FormItem label="STOMP send destination">
            <Input placeholder="e.g. /app/test" style={{ width:'400px' }} value={this.state.stompSendDestination}
                   onChange={this.handleStompSendDestinationChange}
                   disabled={!(this.state.stomp && this.state.connected)}/>
          </FormItem>
        </Form>
      </div>

      {/*要发送的消息*/}
      <div className="message">
        <Form inline>
          <FormItem label="Message Content">
            <Input placeholder="message content sent to server" style={{ width:'290px' }}
                   value={this.state.messageContent} onChange={this.handleMessageContentChange}
                   disabled={!this.state.connected}/>
          </FormItem>
          <FormItem>
            <Button type="primary" disabled={!this.state.connected} onClick={this.send}>Send</Button>
          </FormItem>
          <FormItem>
            <Button type="primary" onClick={this.clearOutput}>Clear Output</Button>
          </FormItem>
        </Form>
      </div>

      {/*console output*/}
      <div className="output">
        <div className="window">
          <div className="body" ref={(div) => {this.consoleOutput = div;}}>
            <pre>
              <div className="comment"># console output</div>

              {this.state.message.length == 0 && <div>$ <span className="pulse">_</span></div>}

              {/*要显示的信息*/}
              {this.state.message.map((item, index) => <div key={index}>
                $&nbsp;
                <span className="command">{item}</span>
              </div>)}
            </pre>
          </div>
        </div>
      </div>

    </div>
  };

}

export default WebSocketDebugTool;
