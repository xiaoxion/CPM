//
//  ViewController.m
//  GrocerList
//
//  Created by Esau Rubio on 11/4/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import "ViewController.h"
#import "RegisterViewController.h"
#import "MainViewController.h"
#import "Reachability.h"
#import "CRToast.h"
#import <Parse/Parse.h>

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    if (![self onCheckConnection]) {
        username.enabled = false;
        password.enabled = false;
        [loginButton setTitle:@"Recheck Connections" forState:UIControlStateNormal];
    }
}

- (void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:true];
    
    PFUser *currentUser = [PFUser currentUser];
    if (currentUser) {
        [self performSegueWithIdentifier:@"loggedIn" sender:self];
    }
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)onClick:(id)sender {
    if ([self onCheckConnection]) {
        UIButton *button = (UIButton*)sender;
        
        if (button.tag == 0) {
            if ([self onCheckConnection] && ![username isEnabled]) {
                [button setTitle:@"Login" forState:UIControlStateNormal];
                username.enabled = true;
                password.enabled = true;
                return;
            }
            
            [PFUser logInWithUsernameInBackground:username.text password:password.text block:^(PFUser *user, NSError *error) {
                if (user) {
                    [self performSegueWithIdentifier:@"loggedIn" sender:self];
                } else {
                    [self onErrorToast:[error userInfo][@"error"]];
                }
            }];
        } else if (button.tag == 1) {
            [self performSegueWithIdentifier:@"register" sender:self];
        }
    } else {
        username.enabled = false;
        password.enabled = false;
        
        [loginButton setTitle:@"Recheck Connections" forState:UIControlStateNormal];
        [self onErrorToast:@"Check Network Connection"];
    }
}

- (void)onErrorToast:(NSString *)errorString {
    NSDictionary *options = @{
                              kCRToastTextKey : errorString,
                              kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                              kCRToastBackgroundColorKey : [UIColor redColor],
                              kCRToastAnimationInTypeKey : @(CRToastAnimationTypeSpring),
                              kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeLinear),
                              kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionTop),
                              kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionTop),
                              kCRToastAnimationInTimeIntervalKey : @0.25,
                              kCRToastTimeIntervalKey : @0.75
                              };
    
    [CRToastManager showNotificationWithOptions:options completionBlock:nil];
}

-(BOOL)onCheckConnection {
    Reachability *check = [Reachability reachabilityForInternetConnection];
    NetworkStatus netStatus = [check currentReachabilityStatus];
    if (netStatus == NotReachable) {
        return false;
    } else {
        return true;
    }
}

@end
