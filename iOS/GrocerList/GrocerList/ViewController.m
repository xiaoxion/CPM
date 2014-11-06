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
#import <Parse/Parse.h>

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
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
    UIButton *button = (UIButton*)sender;
    
    if (button.tag == 0) {
        [PFUser logInWithUsernameInBackground:username.text password:password.text block:^(PFUser *user, NSError *error) {
            if (user) {
                [self performSegueWithIdentifier:@"loggedIn" sender:self];
            }
        }];
    } else if (button.tag == 1) {
        [self performSegueWithIdentifier:@"register" sender:self];
    }
    
}

@end
